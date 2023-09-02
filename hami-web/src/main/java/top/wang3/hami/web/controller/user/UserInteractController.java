package top.wang3.hami.web.controller.user;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.security.model.Result;

/**
 * 用户交互 关注 点赞 收藏 评论
 */
@RestController
@RequestMapping("/api/v1/interact")
public class UserInteractController {


    @PostMapping("/follow/do")
    public Result<Void> doFollow() {
        return Result.success();
    }
}
