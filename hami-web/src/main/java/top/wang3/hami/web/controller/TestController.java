package top.wang3.hami.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.security.annotation.RateLimit;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/test")
@RateLimit
public class TestController {

    @RequestMapping("/hello")
    public Result<Void> hello() {
        return Result.success("hello");
    }

    @RequestMapping("/hello2")
    public Result<Void> hello2() {
        return Result.success("hello2");
    }
}
