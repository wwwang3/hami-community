package top.wang3.hami.web.controller.image;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.core.service.common.ImageService;
import top.wang3.hami.security.model.Result;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

/**
 * image
 * 图像上传接口
 */
@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 图像上传
     * @param image 图像文件
     * @param type 图片关联的类型 1-文章图片 2-用户头像
     * @return 图像访问地址
     */
    @PostMapping("/upload")
    @RateLimit(capacity = 216, interval = 86400L, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public Result<String> upload(@RequestPart("image") MultipartFile image, @RequestParam("type") String type) {
        String url = imageService.upload(image, type);
        return Result.ofNullable(url)
                .orElse("上传失败");
    }
}
