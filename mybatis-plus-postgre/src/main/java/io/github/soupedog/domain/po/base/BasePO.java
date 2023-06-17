package io.github.soupedog.domain.po.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.soupedog.dao.handler.AutoUpdateTimestampOfCreateAndUpdateMetaObjectHandler;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.reflection.MetaObject;

import java.sql.Timestamp;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Getter
@Setter
@Generated
public abstract class BasePO {
    /**
     * 无法光靠注解生效，需要配合 {@link MetaObjectHandler}
     *
     * @see AutoUpdateTimestampOfCreateAndUpdateMetaObjectHandler#insertFill(MetaObject)
     */
    @TableField(fill = FieldFill.INSERT)
    protected Timestamp createTs;
    /**
     * 无法光靠注解生效，需要配合 {@link MetaObjectHandler}
     *
     * @see AutoUpdateTimestampOfCreateAndUpdateMetaObjectHandler#insertFill(MetaObject)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected Timestamp lastUpdateTs;
}
