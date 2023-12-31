package top.wang3.hami.web.controller.interact;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.model.Result;

import java.util.Map;

/**
 * 用户交互 关注 点赞 收藏 评论
 */
@RestController
@RequestMapping("/api/v1/interact")
@RequiredArgsConstructor
public class UserStatController {

    private final CountService countService;

    @GetMapping("/data_growing")
    public Result<Map<String, Integer>> dataGrowing() {
        int loginUserId = LoginUserContext.getLoginUserId();
        Map<String, Integer> result = countService.getUserDailyDataGrowing(loginUserId);
        return Result.of(result);
    }

}
