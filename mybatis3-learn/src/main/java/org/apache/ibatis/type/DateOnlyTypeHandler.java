package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @description 日期Date类型转换器
 * @author mrjimmylin
 * @date 2019/10/8 14:48
 */
public class DateOnlyTypeHandler extends BaseTypeHandler<Date>{
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
        ps.setDate(i , new java.sql.Date(parameter.getTime()));
    }

    @Override
    public Date getNullableResult(ResultSet rs, String columnName) throws SQLException {
        java.sql.Date date = rs.getDate(columnName);
        return date != null ? new Date(date.getTime()) : null;
    }

    @Override
    public Date getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        java.sql.Date date = rs.getDate(columnIndex);
        return date != null ? new Date(date.getTime()) : null;
    }

    @Override
    public Date getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        java.sql.Date date = cs.getDate(columnIndex);
        return date != null ? new Date(date.getTime()) : null;
    }
}
