package top.wang3.hami.web.controller.notify;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.notify.ArticleMsgDTO;
import top.wang3.hami.common.dto.notify.CommentMsgDTO;
import top.wang3.hami.common.dto.notify.DiggMsgDTO;
import top.wang3.hami.common.dto.notify.FollowMsgDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.core.service.common.NotifyMsgService;
import top.wang3.hami.security.model.Result;

/**
 * todo: 重构通知消息
 */
@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyMsgService notifyMsgService;

    @GetMapping("/list/system")
    public Result<PageData<NotifyMsg>> listSystemMsg(@RequestParam("pageNum") long pageNum,
                                                     @RequestParam("pageSize") long pageSize) {
        PageData<NotifyMsg> data = notifyMsgService.listSystemMsg(new PageParam(pageNum, pageSize));
        return Result
                .successData(data);
    }

    @GetMapping("/list/system")
    public Result<PageData<CommentMsgDTO>> listCommentMsg(@RequestParam("pageNum") long pageNum,
                                                          @RequestParam("pageSize") long pageSize) {
        PageData<CommentMsgDTO> data = notifyMsgService.listCommentMsg(new PageParam(pageNum, pageSize));
        return Result
                .successData(data);
    }

    @GetMapping("/list/system")
    public Result<PageData<DiggMsgDTO>> listLikeMsg(@RequestParam("pageNum") long pageNum,
                                                     @RequestParam("pageSize") long pageSize) {
        PageData<DiggMsgDTO> data = notifyMsgService.listLikeAndCollectMsg(new PageParam(pageNum, pageSize));
        return Result
                .successData(data);
    }

    @GetMapping("/list/system")
    public Result<PageData<FollowMsgDTO>> listFollowMsg(@RequestParam("pageNum") long pageNum,
                                                     @RequestParam("pageSize") long pageSize) {
        PageData<FollowMsgDTO> data = notifyMsgService.listFollowingMsg(new PageParam(pageNum, pageSize));
        return Result
                .successData(data);
    }

    @GetMapping("/list/system")
    public Result<PageData<ArticleMsgDTO>> listArticleMsg(@RequestParam("pageNum") long pageNum,
                                                     @RequestParam("pageSize") long pageSize) {
        PageData<ArticleMsgDTO> data = notifyMsgService.listArticleMsg(new PageParam(pageNum, pageSize));
        return Result
                .successData(data);
    }

}
