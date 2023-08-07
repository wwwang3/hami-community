package top.wang3.hami.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/hello")
    public String hello() {
        return "hello world!";
    }

    @RequestMapping("/")
    public String index() {
        return "hello Hami!";
    }

}
