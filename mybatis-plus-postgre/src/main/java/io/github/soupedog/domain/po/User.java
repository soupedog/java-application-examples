package io.github.soupedog.domain.po;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.soupedog.domain.enums.UserSexEnum;
import io.github.soupedog.domain.enums.UserStateEnum;
import io.github.soupedog.domain.po.base.BasePO;
import io.github.soupedog.domain.po.inner.UserConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(schema = "local_test", value = "user")
public class User extends BasePO {
    @TableId(type = IdType.AUTO)
    private Long uid;
    /**
     * 用于演示自增字段如何使用
     */
    private Long sequence;
    /**
     * 用于演示特殊数据库字段和 PO 属性映射，此处为数据库大写字段映射到小驼峰 PO 实体
     */
    @TableField("\"NAME\"")
    private String name;
    private BigDecimal balance;
    @EnumValue
    private UserSexEnum userSex;
    @EnumValue
    private UserStateEnum userState;
    private UserConfiguration configuration;
}
