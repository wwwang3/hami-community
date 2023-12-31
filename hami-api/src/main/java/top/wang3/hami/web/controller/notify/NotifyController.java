package top.wang3.hami.web.controller.notify;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.core.service.notify.NotifyMsgService;
import top.wang3.hami.security.model.Result;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyMsgService notifyMsgService;

    @PostMapping("/reply/query_list")
    public Result<PageData<NotifyMsgDTO>> listCommentNotify(@RequestBody @Valid PageParam param)  {
        PageData<NotifyMsgDTO> msgs = notifyMsgService.listCommentNotify(param);
        return Result.successData(msgs);
    }

    @PostMapping("/love/query_list")
    public Result<PageData<NotifyMsgDTO>> listLikeCollectNotify(@RequestBody @Valid PageParam param)  {
        PageData<NotifyMsgDTO> msgs = notifyMsgService.listLikeCollectNotify(param);
        return Result.successData(msgs);
    }

    @PostMapping("/follow/query_list")
    public Result<PageData<NotifyMsgDTO>> listFollowNotify(@RequestBody @Valid PageParam param)  {
        PageData<NotifyMsgDTO> msgs = notifyMsgService.listFollowNotify(param);
        return Result.successData(msgs);
    }

    @PostMapping("/system/query_list")
    public Result<PageData<NotifyMsgDTO>> listSystemMsg(@RequestBody @Valid PageParam param) {
        PageData<NotifyMsgDTO> msgs = notifyMsgService.listSystemMsg(param);
        return Result.successData(msgs);
    }

    @GetMapping("/count")
    public Result<Map<Integer, Integer>> getUnreadNotify() {
        Map<Integer, Integer> unRead = notifyMsgService.getNoReadNotify();
        return Result.successData(unRead);
    }

    @PostMapping("/delete")
    public Result<Void> deleteNotify(@RequestParam("msg_id") Integer msgId) {
        boolean deleted = notifyMsgService.deleteNotify(msgId);
        return Result.ofTrue(deleted)
                .orElse("删除失败");
    }
}
