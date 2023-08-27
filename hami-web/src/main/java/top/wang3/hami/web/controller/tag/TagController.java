package top.wang3.hami.web.controller.tag;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.request.PageParam;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.security.model.Result;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/all")
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return Result.success(tags);
    }

    @GetMapping("/get")
    public Result<PageData<Tag>> getTagsByPage(@RequestParam("pageNum") long pageNum,
                                               @RequestParam("pageSize") long pageSize) {
        PageData<Tag> tags = tagService.getTagByPage(new PageParam(pageNum, pageSize));
        return Result.success(tags);
    }

}
