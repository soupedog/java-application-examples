package io.github.soupedog.jpa.domain.po.base;

import hygge.util.UtilCreator;
import hygge.util.definition.ParameterHelper;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

/**
 * "@Type(type = "json")" 、"@TypeDef(name = "json", typeClass = JsonType.class)" 可移除 <br/>
 * 因为 hibernate 6 原生提供了 "@JdbcTypeCode(SqlTypes.JSON)" 无需依赖三方库实现 PO 存储内嵌对象，当前是演示 hibernate 5
 *
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@TypeDef(name = "json", typeClass = JsonType.class)
@Getter
@Setter
@Generated
@MappedSuperclass
public abstract class BasePO {
    protected static final ParameterHelper parameterHelper = UtilCreator.INSTANCE.getDefaultInstance(ParameterHelper.class);

    protected BasePO() {
    }

    /**
     * 创建时自动更新时间
     * <p>
     * 需配合如 {@link JpaRepository} 实例提供的语法糖操作才可生效，自定义的原生 sql 自动更新会失效
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "datetime(3)")
    protected Timestamp createTs;
    /**
     * 修改时自动更新时间
     * <p>
     * 需配合如 {@link JpaRepository} 实例提供的语法糖操作才可生效，自定义的原生 sql 自动更新会失效
     */
    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "datetime(3)")
    protected Timestamp lastUpdateTs;
}
