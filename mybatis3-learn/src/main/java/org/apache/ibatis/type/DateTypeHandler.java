package org.apache.ibatis.type;

import java.sql.*;
import java.util.Date;

/**
 * @author mrjimmylin
 * @description 日期类型处理器  Date <===> TimeStamp
 * @date 2019/10/8 14:42
 */
public class DateTypeHandler extends BaseTypeHandler<Date> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
        ps.setTimestamp(i, new Timestamp(parameter.getTime()));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp sqlTimeStamp = rs.getTimestamp(columnName);
        return sqlTimeStamp != null ? new Date(sqlTimeStamp.getTime()) : null;
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp sqlTimeStamp = rs.getTimestamp(columnIndex);
        return sqlTimeStamp != null ? new Date(sqlTimeStamp.getTime()) : null;
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp sqlTimeStamp = cs.getTimestamp(columnIndex);
        return sqlTimeStamp != null ? new Date(sqlTimeStamp.getTime()) : null;
    }
}
