package io.github.soupedog.dao.handler;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zaxxer.hikari.HikariDataSource;
import io.github.soupedog.config.DatabaseConfig;
import org.apache.ibatis.reflection.MetaObject;

import javax.sql.DataSource;
import java.sql.Timestamp;

/**
 * 元数据填充工具，需要注册后才能生效
 *
 * @author Xavier
 * @date 2023/6/08
 * @see DatabaseConfig#sqlSessionFactory(HikariDataSource)
 * @see MybatisPlusAutoConfiguration#sqlSessionFactory(DataSource)
 * @since 1.0
 */
public class AutoUpdateTimestampOfCreateAndUpdateMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自主填充 Po 对象 中名为 createTs/lastUpdateTs 的字段
        Timestamp currentTs = new Timestamp(System.currentTimeMillis());
        this.setFieldValByName("createTs", currentTs, metaObject);
        this.setFieldValByName("lastUpdateTs", currentTs, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 自主填充 Po 对象 中名为 lastUpdateTs 的字段
        Timestamp currentTs = new Timestamp(System.currentTimeMillis());
        this.setFieldValByName("lastUpdateTs", currentTs, metaObject);
    }
}
