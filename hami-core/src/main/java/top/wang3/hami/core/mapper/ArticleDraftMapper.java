package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wang3.hami.common.dto.ArticleDraftDTO;
import top.wang3.hami.common.model.ArticleDraft;

import java.util.List;

@Mapper
public interface ArticleDraftMapper extends BaseMapper<ArticleDraft> {

    /**
     * 查询文章草稿列表
     * @param page 分页参数
     * @param userId 用户ID
     * @param state 状态 0-代表没有发布的文章 1-已经发表的文章
     * @return List<ArticleDraftDTO>
     */
    List<ArticleDraftDTO> selectArticleDraftsByUserId(Page<ArticleDraftDTO> page,
                                                      @Param("userId") int userId, @Param("state") byte state);
}