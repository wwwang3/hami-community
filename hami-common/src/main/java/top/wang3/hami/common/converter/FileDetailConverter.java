package top.wang3.hami.common.converter;


import cn.xuyanwu.spring.file.storage.FileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.model.FileDetail;

@Mapper
public interface FileDetailConverter {

    FileDetailConverter INSTANCE = Mappers.getMapper(FileDetailConverter.class);

    FileDetail toFileDetail(FileInfo fileInfo);

    @Mapping(target = "thUrl", ignore = true)
    @Mapping(target = "thSize", ignore = true)
    @Mapping(target = "thFilename", ignore = true)
    @Mapping(target = "thFileAcl", ignore = true)
    @Mapping(target = "thContentType", ignore = true)
    @Mapping(target = "fileAcl", ignore = true)
    @Mapping(target = "attr", ignore = true)
    FileInfo toFileInfo(FileDetail detail);
}
