package io.github.soupedog.mybatis.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 用于演示非与表完整对应的 PO 对象
 *
 * @author Xavier
 * @date 2024/9/05
 * @since 1.0
 */
@Getter
@Setter
@Builder
@Generated
@NoArgsConstructor
@AllArgsConstructor
public class UserSimple {
    private Long uid;
    private BigDecimal balance;
    private String description = "扣字儿表明自己是一个 UserSimple 实例";
}
