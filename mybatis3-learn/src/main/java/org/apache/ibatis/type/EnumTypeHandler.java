package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @description 枚举类型转成String类型  enum.name <====> string
 * @author mrjimmylin
 * @date 2019/10/8 15:00
 */
public class EnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    // 枚举类型
    private final Class<E> type;

    public EnumTypeHandler(Class<E> type) {
        if(type == null) throw new IllegalArgumentException("枚举类型不能为空！！");
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        if(jdbcType == null){
            ps.setString(i, parameter.name());
        }else{
            ps.setObject(i, parameter.name(), jdbcType.TYPE_CODE);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return s == null ? null : Enum.valueOf(type, s);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return s == null ? null : Enum.valueOf(type, s);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return s == null ? null : Enum.valueOf(type, s);
    }
}
