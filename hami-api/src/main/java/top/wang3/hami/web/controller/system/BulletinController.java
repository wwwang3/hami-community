package top.wang3.hami.web.controller.system;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.Bulletin;
import top.wang3.hami.core.service.system.BulletinService;
import top.wang3.hami.security.annotation.PublicApi;
import top.wang3.hami.security.model.Result;

/**
 * system
 * 阅读记录
 */
@RestController
@RequestMapping("/api/v1/bulletin")
@RequiredArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;

    /**
     * 获取公告列表
     *
     * @param param 分页参数{ @link PageParam}
     * @return {@link PageData<Bulletin>}
     */
    @PostMapping("/query_list")
    @PublicApi
    public Result<PageData<Bulletin>> listBulletinByPage(@RequestBody PageParam param) {
        return Result.of(bulletinService.listBulletinByPage(param));
    }

    /**
     * 获取最新公告
     *
     * @return {@link Bulletin}
     */
    @GetMapping("/new")
    @PublicApi
    public Result<Bulletin> getNewestBulletin() {
        return Result.of(bulletinService.getNewstBulletin());
    }

}
