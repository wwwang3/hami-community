package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.wang3.hami.common.model.ReadingRecord;

import java.util.List;

@Mapper
public interface ReadingRecordMapper extends BaseMapper<ReadingRecord> {

    @Update(
            value = """
                        INSERT INTO reading_record (user_id, article_id) VALUES(#{user_id}, #{article_id})
                        ON DUPLICATE KEY UPDATE reading_time = NOW(3);
                    """
    )
    int record(@Param("user_id") int userId, @Param("article_id") int articleId);

    List<ReadingRecord> selectReadingRecordByKeyword(Page<ReadingRecord> page,
                                                     @Param("user_id") Integer userId,
                                                     @Param("keyword") String keyword);
}