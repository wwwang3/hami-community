package top.wang3.hami.common.vo.article;

import lombok.Data;
import top.wang3.hami.common.model.ArticleDraft;
import top.wang3.hami.common.vo.user.UserVo;

@Data
public class ArticleDraftVo {
    private ArticleDraft draft;
    private UserVo user;
}
