package top.wang3.hami.web.controller.image;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.core.service.common.ImageService;
import top.wang3.hami.security.model.Result;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;


    @PostMapping("/upload")
    public Result<String> upload(@RequestPart("image") MultipartFile image, @RequestParam("type") String type) {
        String url = imageService.upload(image, type);
        return Result.ofNullable(url)
                .orElse("上传失败");
    }
}
