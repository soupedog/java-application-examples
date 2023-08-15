package io.github.soupedog.mybatis.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.soupedog.mybatis.domain.po.base.BasePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Xavier
 * @date 2023/6/16
 * @since 1.0
 */
@Getter
@Setter
@Builder
@Generated
@NoArgsConstructor
@AllArgsConstructor
@TableName(schema = "local_test", value = "house")
public class House extends BasePO {
    /**
     * 用于演示序列如何使用
     */
    @TableId(type = IdType.INPUT)
    private Long hid;
    private String location;
}
