package top.wang3.hami.core.service.article.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.canal.CanalEntryHandler;
import top.wang3.hami.common.canal.annotation.CanalListener;
import top.wang3.hami.common.model.Article;


@Component
@CanalListener(value = "article")
@RequiredArgsConstructor
@Slf4j
public class ArticleCountHandler implements CanalEntryHandler<Article> {

    @Override
    public void processInsert(Article entity) {

    }

    @Override
    public void processUpdate(Article before, Article after) {

    }

    @Override
    public void processDelete(Article deletedEntity) {

    }
}
