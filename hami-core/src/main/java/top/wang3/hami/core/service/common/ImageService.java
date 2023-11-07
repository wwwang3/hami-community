package top.wang3.hami.core.service.common;

import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.FileStorageService;
import jakarta.annotation.Resource;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.wang3.hami.core.exception.HamiServiceException;
import top.wang3.hami.security.context.LoginUserContext;
import top.wang3.hami.security.ratelimit.annotation.RateLimit;

import java.io.InputStream;
import java.util.function.Consumer;

@Service
public class ImageService {


    @Resource
    FileStorageService fileStorageService;


    public String upload(MultipartFile file, String type) {
        return upload(file, type, null);
    }

    @RateLimit(capacity = 216, rate = 0.0025, scope = RateLimit.Scope.LOGIN_USER,
            algorithm = RateLimit.Algorithm.FIXED_WINDOW)
    public String upload(MultipartFile file, String type, Consumer<Thumbnails.Builder<? extends InputStream>> consumer) {
        if (file == null) {
            throw new HamiServiceException("参数错误");
        }
        int id = LoginUserContext.getLoginUserId();
        FileInfo info = fileStorageService.of(file)
                .setObjectId(id)
                .setObjectType(type)
                .image(consumer != null, consumer)
                .upload();
        return info.getUrl();
    }
}
