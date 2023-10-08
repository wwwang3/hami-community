package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.wang3.hami.common.model.ReadingRecord;

@Mapper
public interface ReadingRecordMapper extends BaseMapper<ReadingRecord> {

    @Update(
            value = """
                        INSERT INTO reading_record (user_id, article_id) VALUES(#{user_id}, #{article_id})
                        ON DUPLICATE KEY UPDATE reading_time = NOW(3);
                    """
    )
    int record(@Param("user_id") int userId, @Param("article_id") int articleId);
}