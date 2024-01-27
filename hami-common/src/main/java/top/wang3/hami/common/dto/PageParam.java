package top.wang3.hami.common.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基本分页参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParam {

    /**
     * 当前页数
     */
    @Min(value = 1)
    private long current;

    /**
     * 元素个数
     */
    @Min(value = 5)
    @Max(value = 20)
    private long size;

    public <T> Page<T> toPage() {
        return toPage(true);
    }

    public <T> Page<T> toPage(boolean searchCount) {
        return Page.of(current, size, searchCount);
    }
}
