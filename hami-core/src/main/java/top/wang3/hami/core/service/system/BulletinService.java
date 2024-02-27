package top.wang3.hami.core.service.system;

import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.Bulletin;

public interface BulletinService {

    PageData<Bulletin> listBulletinByPage(PageParam param);

    Bulletin getNewstBulletin();

}
