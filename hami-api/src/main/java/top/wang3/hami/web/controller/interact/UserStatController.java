package top.wang3.hami.web.controller.interact;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.dto.interact.DataGrowing;
import top.wang3.hami.core.service.stat.CountService;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.model.Result;

import java.util.Map;

/**
 * interact
 * 用户数据变化
 */
@RestController
@RequestMapping("/api/v1/interact")
@RequiredArgsConstructor
public class UserStatController {

    private final CountService countService;

    /**
     * 用户昨日数据变化
     * @response {@link DataGrowing}
     */
    @GetMapping("/data_growing")
    public Result<Map<String, Integer>> dataGrowing() {
        int loginUserId = LoginUserContext.getLoginUserId();
        Map<String, Integer> result = countService.getUserYesterdayDataGrowing(loginUserId);
        return Result.of(result);
    }

}
