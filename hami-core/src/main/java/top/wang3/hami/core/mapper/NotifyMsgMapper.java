package top.wang3.hami.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.wang3.hami.common.model.NotifyCount;
import top.wang3.hami.common.model.NotifyMsg;
import top.wang3.hami.common.vo.notify.NotifyMsgVo;

import java.util.List;

@Mapper
public interface NotifyMsgMapper extends BaseMapper<NotifyMsg> {

    Page<NotifyMsgVo> listCommentNotify(Page<NotifyMsgVo> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgVo> listLoveNotify(Page<NotifyMsgVo> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgVo> listFollowNotify(Page<NotifyMsgVo> page, @Param("receiver") Integer receiver);

    Page<NotifyMsgVo> listSystemNotifyMsg(Page<NotifyMsgVo> page, @Param("receiver") Integer receiver);


    List<NotifyCount> selectNoReadNotify(@Param("receiver") Integer receiver);

    @Update(
            value = """
                        INSERT INTO notify_msg (item_id, related_id, sender, receiver, type, detail)
                        VALUES(#{itemId}, #{relatedId}, #{sender}, #{receiver}, #{type}, #{detail})
                        ON DUPLICATE KEY UPDATE mtime = NOW(3);
                    """
    )
    int saveNotifyMsg(NotifyMsg msg);
}