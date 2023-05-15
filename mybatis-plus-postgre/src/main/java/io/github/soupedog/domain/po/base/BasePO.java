package io.github.soupedog.domain.po.base;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Getter
@Setter
public abstract class BasePO {
    protected Timestamp createTs;
    protected Timestamp lastUpdateTs;
}
