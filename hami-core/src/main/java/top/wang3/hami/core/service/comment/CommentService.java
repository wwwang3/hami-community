package top.wang3.hami.core.service.comment;

import top.wang3.hami.common.dto.CommentDTO;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.PageParam;

public interface CommentService {


    PageData<CommentDTO> getComments(PageParam)
}
