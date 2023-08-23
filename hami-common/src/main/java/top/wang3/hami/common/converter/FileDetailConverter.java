package top.wang3.hami.common.converter;


import cn.xuyanwu.spring.file.storage.FileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.model.FileDetail;

@Mapper
public interface FileDetailConverter {

    FileDetailConverter INSTANCE = Mappers.getMapper(FileDetailConverter.class);

    FileDetail toFileDetail(FileInfo fileInfo);

    FileInfo toFileInfo(FileDetail detail);
}
