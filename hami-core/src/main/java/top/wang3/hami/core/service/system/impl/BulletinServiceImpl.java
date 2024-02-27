package top.wang3.hami.core.service.system.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wang3.hami.common.dto.PageData;
import top.wang3.hami.common.dto.PageParam;
import top.wang3.hami.common.model.Bulletin;
import top.wang3.hami.core.mapper.BulletinMapper;
import top.wang3.hami.core.service.system.BulletinService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulletinServiceImpl implements BulletinService {

    private final BulletinMapper bulletinMapper;

    @Override
    public PageData<Bulletin> listBulletinByPage(PageParam param) {
        Page<Bulletin> page = param.toPage();
        List<Bulletin> list = ChainWrappers.lambdaQueryChain(bulletinMapper)
                .orderByDesc(Bulletin::getCtime)
                .list(page);
        return PageData.build(page, list);
    }

    @Override
    public Bulletin getNewstBulletin() {
        return ChainWrappers.lambdaQueryChain(bulletinMapper)
                .orderByDesc(Bulletin::getCtime)
                .last("limit 1")
                .one();
    }
}
