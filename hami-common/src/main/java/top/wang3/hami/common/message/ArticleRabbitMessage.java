package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.wang3.hami.common.constant.RabbitConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRabbitMessage implements RabbitMessage {

    private Type type;
    private Integer articleId;
    private Integer authorId;
    private Integer loginUserId = null; //当type为view时, 为当前登录用户的ID
    private Integer cateId = null; // 当type为publish or delete时不为空

    public ArticleRabbitMessage(Type type, Integer articleId, Integer authorId) {
        this.type = type;
        this.articleId = articleId;
        this.authorId = authorId;
    }

    public ArticleRabbitMessage(Type type, Integer articleId, Integer authorId, Integer loginUserId) {
        this.type = type;
        this.articleId = articleId;
        this.authorId = authorId;
        this.loginUserId = loginUserId;
    }

    @Override
    public String getExchange() {
        return RabbitConstants.HAMI_ARTICLE_EXCHANGE;
    }

    @Override
    public String getRoute() {
        return type.route;
    }

    @Getter
    public enum Type {
        PUBLISH("article.publish"),
        UPDATE("article.update"),
        DELETE("article.delete"),
        VIEW("article.view");

        public final String route;

        Type(String route) {
            this.route = route;
        }


    }
}
