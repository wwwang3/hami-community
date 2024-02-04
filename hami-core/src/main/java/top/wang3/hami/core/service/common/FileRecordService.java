package top.wang3.hami.core.service.common;


import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.recorder.FileRecorder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.converter.FileDetailConverter;
import top.wang3.hami.common.model.FileDetail;
import top.wang3.hami.core.mapper.FileDetailMapper;

/**
 * 保存文件记录
 */
@Service
@RequiredArgsConstructor
public class FileRecordService extends ServiceImpl<FileDetailMapper, FileDetail> implements FileRecorder {

    private final ThreadPoolTaskExecutor taskExecutor;

    @Override
    public boolean save(FileInfo fileInfo) {
        taskExecutor.submit(() -> {
            FileDetail detail = FileDetailConverter.INSTANCE.toFileDetail(fileInfo);
            super.save(detail);
        });
        return true;
    }

    @Override
    public FileInfo getByUrl(String url) {
        FileDetail detail = ChainWrappers.queryChain(getBaseMapper())
                .eq("url", url)
                .one();
        return FileDetailConverter.INSTANCE.toFileInfo(detail);
    }

    @Override
    public boolean delete(String url) {
        QueryWrapper<FileDetail> wrapper = Wrappers.query(getEntityClass())
                .eq("url", url);
        return super.remove(wrapper);
    }
}
