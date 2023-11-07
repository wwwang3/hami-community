package top.wang3.hami.common.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 1)
    private long pageNum;

    /**
     * 元素个数
     */
    @Min(value = 5)
    @Max(value = 20)
    private long pageSize;

    public <T> Page<T> toPage() {
        return toPage(true);
    }

    public <T> Page<T> toPage(boolean searchCount) {
        return Page.of(pageNum, pageSize, searchCount);
    }
}
