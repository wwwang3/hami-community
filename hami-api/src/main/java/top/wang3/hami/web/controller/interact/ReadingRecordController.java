package top.wang3.hami.web.controller.interact;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.SearchParam;
import top.wang3.hami.common.vo.article.ReadingRecordVo;
import top.wang3.hami.core.service.interact.ReadingRecordService;
import top.wang3.hami.security.model.Result;


/**
 * interact
 * 阅读记录
 */
@RestController
@RequestMapping("/api/v1/interact/")
@RequiredArgsConstructor
public class ReadingRecordController {

    private final ReadingRecordService readingRecordService;

    /**
     * 分页获取阅读记录
     * @param param {@link SearchParam}
     * @return {@link PageData<ReadingRecordVo>}
     */
    @PostMapping("/reading_record/query_list")
    public Result<PageData<ReadingRecordVo>> listReadingRecord(@RequestBody
                                                                @Valid SearchParam param) {
        PageData<ReadingRecordVo> pageData = readingRecordService
                .listReadingRecords(param);
        return Result.ofNullable(pageData)
                .orElse("还没有历史记录");
    }

    /**
     * 删除阅读记录
     * @param id 阅读记录ID
     * @return 空
     */
    @PostMapping("/reading_record/delete")
    public Result<Void> deleteReadingRecord(@RequestParam("record_id") Integer id) {
        boolean deleted = readingRecordService.deleteRecord(id);
        return Result.ofTrue(deleted)
                .orElse("操作失败");
    }

    /**
     * 清空历史记录
     * @return 空
     */
    @PostMapping("/reading_record/clear")
    public Result<Void> clearReadingRecords() {
        boolean deleted = readingRecordService.clearReadingRecords();
        return Result.ofTrue(deleted)
                .orElse("操作失败");
    }
}
