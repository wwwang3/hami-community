package top.wang3.hami.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRabbitMessage implements RabbitMessage {

    private int type;
    private Integer articleId;

    @Override
    public String getRoute() {
        if (type == 1) {
            return "article.publish";
        } else if (type == 2) {
            return "article.update";
        } else {
            return "article.delete";
        }
    }
}
