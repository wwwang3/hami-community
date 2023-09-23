package top.wang3.hami.core.service.stat.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.FollowCountItem;
import top.wang3.hami.common.dto.article.ArticleStatDTO;
import top.wang3.hami.common.dto.user.UserStat;
import top.wang3.hami.common.util.ListMapperHandler;
import top.wang3.hami.core.repository.ArticleStatRepository;
import top.wang3.hami.core.service.article.ArticleStatService;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.core.service.user.UserFollowService;

import java.util.List;


@Service("simpleCountService")
@RequiredArgsConstructor
public class SimpleCountService implements CountService {

    private final ArticleStatService articleStatService;
    private final UserFollowService userFollowService;
    private final ArticleStatRepository articleStatRepository;

    @Override
    public ArticleStatDTO getArticleStatById(int articleId) {
        return articleStatService.getArticleStatByArticleId(articleId);
    }

    @Override
    public UserStat getUserStatById(Integer userId) {
        UserStat stat = articleStatRepository.getUserStatByUserId(userId);
        Integer followings = userFollowService.getUserFollowingCount(userId);
        Integer followers = userFollowService.getUserFollowerCount(userId);
        stat.setTotalFollowings(followings);
        stat.setTotalFollowers(followers);
        return stat;
    }

    @Override
    public List<ArticleStatDTO> getArticleStatByIds(List<Integer> articleIds) {
        return articleStatService.getArticleStatByArticleIds(articleIds);
    }

    @Override
    public List<UserStat> getUserStatByUserIds(List<Integer> userIds) {
        List<UserStat> stats = articleStatRepository.getUserStatByUserIds(userIds);
        List<FollowCountItem> followings = userFollowService.getUserFollowerCount(userIds);
        List<FollowCountItem> followers = userFollowService.getUserFollowerCount(userIds);
        ListMapperHandler.doAssemble(stats, UserStat::getUserId, followings, FollowCountItem::getUserId, (stat, item) -> {
            stat.setTotalFollowings(item.getCount());
        });
        ListMapperHandler.doAssemble(stats, UserStat::getUserId, followers, FollowCountItem::getUserId, (stat, item) -> {
            stat.setTotalFollowers(item.getCount());
        });
        return stats;
    }
}
