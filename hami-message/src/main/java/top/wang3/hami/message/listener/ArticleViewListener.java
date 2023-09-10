package top.wang3.hami.message.listener;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.constant.Constants;
import top.wang3.hami.common.model.ArticleStat;
import top.wang3.hami.core.service.common.CountService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RabbitListener(queues = Constants.ADD_VIEWS_QUEUE,
        messageConverter = "simpleMessageConverter", containerFactory = "batchRabbitContainerFactory",
        concurrency = "2")
@RequiredArgsConstructor
public class ArticleViewListener {

    private final CountService countService;

    @RabbitHandler
    public void handleMessage(List<Integer> views) {
        Map<Integer, ArticleStat> map = new HashMap<Integer, ArticleStat>();
        for (Integer articleId : views) {

            ArticleStat stat = map.computeIfAbsent(articleId, (id) -> {
                ArticleStat newStat = new ArticleStat();
                newStat.setArticleId(id);
                newStat.setViews(0);
                return newStat;
            });
            stat.setViews(stat.getViews() + 1);
        }
        Collection<ArticleStat> stats = map.values();
        countService.increaseViews(stats);
    }
}
