package io.github.soupedog.jpa.domain.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.soupedog.jpa.domain.po.base.BasePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.cfg.AvailableSettings;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

/**
 * 简单阐述下演示对象关系： {@link User}——用户 发起 {@link CapitalPool}——资金池，{@link Investor}——投资人 注资资金池
 *
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
@Table(name = "capitalPool")
public class CapitalPool extends BasePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cpId;
    private String name;
    /**
     * 用 capitalPool_investor_mapping 的新表保存 资金池-投资人 关联关系(即便调整了 {@link AvailableSettings#HBM2DDL_DEFAULT_CONSTRAINT_MODE}，此处仍会强制生成数据库层面的外键)
     * <p>
     * {@link JsonIgnore} 是防止序列化/反序列化循环 Investor-CapitalPool 互相持有对方
     */
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "capitalPool_investor_mapping",
            // name:当前对象关联字段在中间表的名称  referencedColumnName:当前对象关联字段名称
            joinColumns = {@JoinColumn(name = "cpId_inDB", referencedColumnName = "cpId")},
            // 与 joinColumns 含义相同，只不过是描述关联关系的另一方
            inverseJoinColumns = {@JoinColumn(name = "iid_inDB", referencedColumnName = "iid")}
    )
    private List<Investor> investors;
    /**
     * CapitalPool 表存 User 的 uid 做关联(调整了 {@link AvailableSettings#HBM2DDL_DEFAULT_CONSTRAINT_MODE} 所以此处不会创建数据库层面的外键，但插入时会自动查询 User 表进行验证)<br/>
     */
    @ManyToOne
    @JoinColumn(name = "uid")
    private User founder;
}
