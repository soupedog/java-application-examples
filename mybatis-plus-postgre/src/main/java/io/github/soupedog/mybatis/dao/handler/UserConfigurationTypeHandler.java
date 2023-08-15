package io.github.soupedog.mybatis.dao.handler;

import hygge.util.UtilCreator;
import hygge.util.definition.JsonHelper;
import io.github.soupedog.mybatis.domain.po.inner.UserConfiguration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 此处含义是将 PO 对象中的嵌套子对象对象作为 json 字符串存储到数据库
 *
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
public class UserConfigurationTypeHandler implements TypeHandler<UserConfiguration> {
    private static final JsonHelper<?> jsonHelper = UtilCreator.INSTANCE.getDefaultJsonHelperInstance(false);

    /**
     * PO 转成数据库格式信息
     */
    @Override
    public void setParameter(PreparedStatement ps, int i, UserConfiguration parameter, JdbcType jdbcType) throws SQLException {
        // 使用者指定不同 jdbcType 的特殊处理逻辑就不演示了，示例代码中不存在手工指定特殊 jdbcType 的场景
        if (parameter != null) {
            ps.setString(i, jsonHelper.formatAsString(parameter));
        }
    }

    /**
     * 只有字段名称时，数据库查询结果转 PO
     */
    @Override
    public UserConfiguration getResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : jsonHelper.readAsObject(value, UserConfiguration.class);
    }

    /**
     * 只有字段序号时，数据库查询结果转 PO
     */
    @Override
    public UserConfiguration getResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : jsonHelper.readAsObject(value, UserConfiguration.class);
    }

    /**
     * 函数/存储过程时数据库字段转对象
     */
    @Override
    public UserConfiguration getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : jsonHelper.readAsObject(value, UserConfiguration.class);
    }
}
