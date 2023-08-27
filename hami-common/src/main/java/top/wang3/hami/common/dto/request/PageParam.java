package top.wang3.hami.common.dto.request;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParam {

    /**
     * 当前页数
     */
    private long pageNum;

    /**
     * 元素个数
     */
    private long pageSize;

    public <T> Page<T> toPage() {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 10;
        }
        return Page.of(pageNum, pageSize);
    }
}
