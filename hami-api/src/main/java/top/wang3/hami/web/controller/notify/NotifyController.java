package top.wang3.hami.web.controller.notify;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.vo.notify.NotifyMsgVo;
import top.wang3.hami.core.service.notify.NotifyMsgService;
import top.wang3.hami.security.model.Result;

import java.util.Map;

/**
 * notify
 * 通知接口
 */
@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyMsgService notifyMsgService;

    /**
     * 查询评论消息
     *
     * @param param {@link PageParam}
     * @return {@link PageData<NotifyMsgVo>}
     */
    @PostMapping("/reply/query_list")
    public Result<PageData<NotifyMsgVo>> listCommentNotify(@RequestBody @Valid PageParam param) {
        PageData<NotifyMsgVo> msgs = notifyMsgService.listCommentNotify(param);
        return Result.successData(msgs);
    }

    /**
     * 查询点赞, 收藏消息
     *
     * @param param {@link PageParam}
     * @return {@link PageData<NotifyMsgVo>}
     */
    @PostMapping("/love/query_list")
    public Result<PageData<NotifyMsgVo>> listLikeCollectNotify(@RequestBody @Valid PageParam param) {
        PageData<NotifyMsgVo> msgs = notifyMsgService.listLikeCollectNotify(param);
        return Result.successData(msgs);
    }

    /**
     * 查询关注消息
     *
     * @param param {@link PageParam}
     * @return {@link PageData<NotifyMsgVo>}
     */
    @PostMapping("/follow/query_list")
    public Result<PageData<NotifyMsgVo>> listFollowNotify(@RequestBody @Valid PageParam param) {
        PageData<NotifyMsgVo> msgs = notifyMsgService.listFollowNotify(param);
        return Result.successData(msgs);
    }

    /**
     * 查询系统消息
     *
     * @param param {@link PageParam}
     * @return {@link PageData<NotifyMsgVo>}
     */
    @PostMapping("/system/query_list")
    public Result<PageData<NotifyMsgVo>> listSystemMsg(@RequestBody @Valid PageParam param) {
        PageData<NotifyMsgVo> msgs = notifyMsgService.listSystemMsg(param);
        return Result.successData(msgs);
    }

    /**
     * 查询未读消息数量
     *
     * @return 每个消息类型未读的数量
     */
    @GetMapping("/count")
    public Result<Map<Integer, Integer>> getUnreadNotify() {
        Map<Integer, Integer> unRead = notifyMsgService.getNoReadNotify();
        return Result.successData(unRead);
    }

    /**
     * 删除消息
     *
     * @param msgId 消息ID
     * @return 空
     */
    @PostMapping("/delete")
    public Result<Void> deleteNotify(@RequestParam("msgId") Integer msgId) {
        boolean deleted = notifyMsgService.deleteNotify(msgId);
        return Result.ofTrue(deleted)
                .orElse("删除失败");
    }
}
