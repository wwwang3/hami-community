package top.wang3.hami.core.init;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.RedisConstants;
import top.wang3.hami.common.constant.TimeoutConstants;
import top.wang3.hami.common.converter.StatConverter;
import top.wang3.hami.common.dto.stat.ArticleStatDTO;
import top.wang3.hami.common.dto.stat.UserStatDTO;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.common.model.UserStat;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.common.util.RedisClient;
import top.wang3.hami.core.mapper.ArticleStatMapper;
import top.wang3.hami.core.mapper.UserStatMapper;

import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
@Order(6)
@RequiredArgsConstructor
public class StatInitializer implements HamiInitializer {

    private final ArticleStatMapper articleStatMapper;
    private final UserStatMapper userStatMapper;

    @Override
    public InitializerEnums getName() {
        return InitializerEnums.STAT_CACHE;
    }

    @Override
    public void run() {
        cacheUserStat();
        cacheArticleStat();
    }

    public void cacheArticleStat() {
        ListMapperHandler.scanDesc(
                Integer.MAX_VALUE,
                1000,
                1000,
                articleStatMapper::scanArticleStatDesc,
                data -> {
                    Map<String, ArticleStatDTO> map = ListMapperHandler.listToMap(
                            data,
                            stat -> RedisConstants.STAT_TYPE_ARTICLE + stat.getArticleId(),
                            StatConverter.INSTANCE::toArticleStatDTO
                    );
                    RedisClient.cacheMultiObject(map, TimeoutConstants.ARTICLE_STAT_EXPIRE, TimeUnit.MILLISECONDS);
                },
                ArticleStat::getArticleId
        );
    }

    public void cacheUserStat() {
        ListMapperHandler.scanDesc(
                Integer.MAX_VALUE,
                100,
                1000,
                userStatMapper::scanUserStatDesc,
                data -> {
                    Map<String, UserStatDTO> map = ListMapperHandler.listToMap(
                            data,
                            stat -> RedisConstants.STAT_TYPE_USER + stat.getUserId(),
                            StatConverter.INSTANCE::toUserStatDTO
                    );
                    RedisClient.cacheMultiObject(map, TimeoutConstants.USER_STAT_EXPIRE, TimeUnit.MILLISECONDS);
                },
                UserStat::getUserId
        );
    }
}
