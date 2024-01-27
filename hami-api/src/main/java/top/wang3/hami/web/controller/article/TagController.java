package top.wang3.hami.web.controller.article;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.Tag;
import top.wang3.hami.core.service.article.TagService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.web.annotation.Public;

import java.util.List;

/**
 * article
 */
@RestController
@RequestMapping("/api/v1/tag")
@Slf4j
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 获取所有标签
     *
     * @return {@link List<Tag>}
     */
    @Public
    @GetMapping("/all")
    public Result<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTag();
        return Result.successData(tags);
    }

    /**
     * 获取标签
     *
     * @param param {@link PageParam}
     * @return {@link PageData<Tag>}
     */
    @Public
    @PostMapping("/query_list")
    public Result<PageData<Tag>> getTagsByPage(@RequestBody @Valid PageParam param) {
        PageData<Tag> tags = tagService.getTagByPage(param);
        return Result.successData(tags);
    }


}
