package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author mrjimmylin
 * @description 枚举类型转换  enum.ordinal <===> int
 * @date 2019/10/8 15:12
 */
public class EnumOrdinalTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    // 枚举类型
    private final Class<E> type;
    // 枚举类型元素
    private final E[] enums;

    public EnumOrdinalTypeHandler(Class<E> type, E[] enums) {
        if (type == null) throw new IllegalArgumentException("枚举类型不能为空！！");
        this.type = type;
        this.enums = type.getEnumConstants();
        if (enums == null) throw new IllegalArgumentException(type.getSimpleName() + " 类 不能表示为一个枚举类！!");
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.ordinal());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int ordinal = rs.getInt(columnName);
        return ordinal == 0 && rs.wasNull() ? null : toOrdinalEnum(ordinal);
    }


    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int ordinal = rs.getInt(columnIndex);
        return ordinal == 0 && rs.wasNull() ? null : toOrdinalEnum(ordinal);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int ordinal = cs.getInt(columnIndex);
        return ordinal == 0 && cs.wasNull() ? null : toOrdinalEnum(ordinal);
    }

    private E toOrdinalEnum(int ordinal) {
        try {
            return enums[ordinal];
        } catch (Exception e) {
            throw new IllegalArgumentException("无法将 ordinal 为 " + ordinal + " 的顺序转换成 " + type.getSimpleName() + " 类型！！");
        }
    }
}
