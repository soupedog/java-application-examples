package io.github.soupedog.jpa.domain.po;

import io.github.soupedog.jpa.domain.enums.UserSexEnum;
import io.github.soupedog.jpa.domain.enums.UserStateEnum;
import io.github.soupedog.jpa.domain.po.base.BasePO;
import io.github.soupedog.jpa.domain.po.inner.UserConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * "@Type(type = "json")" 、"@TypeDef(name = "json", typeClass = JsonType.class)" 可移除 <br/>
 * 因为 hibernate 6 原生提供了 "@JdbcTypeCode(SqlTypes.JSON)" 无需依赖三方库实现 PO 存储内嵌对象，当前是演示 hibernate 5
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
// @Entity 的 name 属性会影响 HQL 的实际名称
@Entity
// 配合 "@Repository" 实例提供的语法糖插入时，PO 的 null 属性不参与
@DynamicInsert
// 配合 "@Repository" 实例提供的语法糖更新时，PO 的 null 属性不参与
@DynamicUpdate
@Table(name = "user", indexes = {@Index(name = "index_name", columnList = "NAME", unique = true)})
public class User extends BasePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    /**
     * 用于演示特殊数据库字段和 PO 属性映射，此处为数据库大写字段映射到小驼峰 PO 实体
     */
    @Column(name = "NAME")
    private String name;
    @Column(columnDefinition = "decimal(12,2)")
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum ('SECRET', 'MAN', 'WOMAN') default 'SECRET'")
    private UserSexEnum userSex;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum ('INACTIVE', 'ACTIVE') default 'ACTIVE'")
    private UserStateEnum userState;
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private UserConfiguration configuration;


    /**
     * 仅用于 HQL 演示
     */
    public User(Long uid, String name, Double balance) {
        this.uid = uid;
        this.name = name;
        this.balance = BigDecimal.valueOf(balance);
    }
}
