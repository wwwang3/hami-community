package top.wang3.hami.web.controller.notify;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.notify.NotifyMsgDTO;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.core.service.notify.NotifyMsgService;
import top.wang3.hami.security.model.Result;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyMsgService notifyMsgService;

    @PostMapping("/comment/query_list")
    public Result<PageData<NotifyMsgDTO>> queryCommentNotify(@RequestBody @Valid PageParam param)  {
        PageData<NotifyMsgDTO> msgs = notifyMsgService.listCommentNotify(param);
        return Result.successData(msgs);
    }

    @PostMapping("/like_collect/query_list")
    public Result<PageData<NotifyMsgDTO>> queryLikeCollectNotify(@RequestBody @Valid PageParam param)  {
        PageData<NotifyMsgDTO> msgs = notifyMsgService.listLikeCollectNotify(param);
        return Result.successData(msgs);
    }

    @PostMapping("/follow/query_list")
    public Result<PageData<NotifyMsgDTO>> queryFollowNotify(@RequestBody @Valid PageParam param)  {
        PageData<NotifyMsgDTO> msgs = notifyMsgService.listFollowNotify(param);
        return Result.successData(msgs);
    }

    @GetMapping("/count")
    public Result<Map<Integer, Integer>> getUnreadNotify() {
        Map<Integer, Integer> unRead = notifyMsgService.getNoReadNotify();
        return Result.successData(unRead);
    }

}
