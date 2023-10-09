package top.wang3.hami.common.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageData<T> {

    /**
     * 当前页数
     */
    private long pageNum;

    /**
     * 元素总数
     */
    private long total;

    /**
     * 数据
     */
    private Collection<T> data;


    public static <T> PageData<T> build(Page<T> page) {
        if (page == null) return PageData.empty();
        return new PageData<>(page.getCurrent(), page.getTotal(), page.getRecords());
    }

    public static <T> PageData<T> empty() {
        return new PageData<>();
    }



}
