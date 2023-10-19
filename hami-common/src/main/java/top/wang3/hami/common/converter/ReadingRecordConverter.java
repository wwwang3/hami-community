package top.wang3.hami.common.converter;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.wang3.hami.common.dto.article.ReadingRecordDTO;
import top.wang3.hami.common.model.ReadingRecord;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ReadingRecordConverter {

    ReadingRecordConverter INSTANCE = Mappers.getMapper(ReadingRecordConverter.class);

    @Mapping(target = "content", ignore = true)
    ReadingRecordDTO toReadingRecordDTO(ReadingRecord record);

    List<ReadingRecordDTO> toReadingRecordDTOList(Collection<ReadingRecord> record);

}
