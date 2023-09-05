package top.wang3.hami.message.handler.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wang3.hami.common.model.Article;
import top.wang3.hami.message.annotation.CanalListener;
import top.wang3.hami.message.handler.CanalEntryHandler;

@Component
@CanalListener(value = "article")
@Slf4j
public class ArticleCanalEntryHandler implements CanalEntryHandler<Article> {

    @Override
    public void processInsert(Article entity) {

    }

    @Override
    public void processUpdate(Article before, Article after) {
        log.debug("update, before: {}, after: {}", before, after);
    }

    @Override
    public void processDelete(Article deletedEntity) {

    }
}
