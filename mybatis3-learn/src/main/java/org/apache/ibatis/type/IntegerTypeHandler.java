package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @description Integer类型处理器
 * @author mrjimmylin
 * @date 2019/10/8 14:26
 */
public class IntegerTypeHandler extends BaseTypeHandler<Integer> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i,parameter);
    }

    @Override
    public Integer getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int result = rs.getInt(columnName);
        // 如果 result 为 0 或者 结果集 为空，则返回 null
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Integer getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int result = rs.getInt(columnIndex);
        // 如果 result 为 0 或者 结果集 为空，则返回 null
        return result == 0 && rs.wasNull() ? null : result;
    }

    @Override
    public Integer getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int result = cs.getInt(columnIndex);
        // 如果 result 为 0 或者 结果集 为空，则返回 null
        return result == 0 && cs.wasNull() ? null : result;
    }
}
