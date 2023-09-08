package top.wang3.hami.common.dto.notify;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticlePublishMsg implements Notify {

    private int articleId;
    private int authorId;
    private String title;

    @Override
    public int getNotifyType() {
        return NotifyType.PUBLISH_ARTICLE.type;
    }
}
