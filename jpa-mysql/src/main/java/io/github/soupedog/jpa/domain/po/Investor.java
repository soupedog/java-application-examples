package io.github.soupedog.jpa.domain.po;

import io.github.soupedog.jpa.domain.po.base.BasePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Getter
@Setter
@Builder
@Generated
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "investor")
public class Investor extends BasePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long iId;
    private String name;
    // Investor 对象放弃维护关联表，只以 CapitalPool.investors 属性的关联关系为准；饿汉模式是为了支持序列化，否则序列化时饿汉模式会导致异常
    @ManyToMany(mappedBy = "investors", fetch = FetchType.EAGER)
    // 集合查询顺序，和写 sql order by 方式一致，也可多字段限定
    @OrderBy(value = "createTs DESC")
    private List<CapitalPool> capitalPools;
}
