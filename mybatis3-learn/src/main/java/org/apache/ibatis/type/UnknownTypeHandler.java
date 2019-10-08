package org.apache.ibatis.type;

import org.apache.ibatis.io.Resources;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

/**
 * @author mrjimmylin
 * @description 未知类型转换器
 * @date 2019/10/8 15:41
 */
public class UnknownTypeHandler extends BaseTypeHandler<Object> {

    // Object类型转换器
    private static final ObjectTypeHandler OBJECT_TYPE_HANDLER = new ObjectTypeHandler();

    // 转换器注册表（所有的类型转换器）
    private TypeHandlerRegistry typeHandlerRegistry;

    public UnknownTypeHandler(TypeHandlerRegistry typeHandlerRegistry) {
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        // 获得参数对应的处理器
        TypeHandler handler = resolveTypeHandler(parameter, jdbcType);
        handler.setParameter(ps, i, parameter, jdbcType);
    }


    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        TypeHandler<?> handler = resolveTypeHandler(rs, columnName);
        return handler.getResult(rs, columnName);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        TypeHandler<?> handler = resolveTypeHandler(rs.getMetaData(), columnIndex);
        if (handler == null || handler instanceof UnknownTypeHandler) {
            handler = OBJECT_TYPE_HANDLER;
        }
        return handler.getResult(rs, columnIndex);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getObject(columnIndex);
    }

    // 根据 parameter 获取该参数的类型转换器
    private TypeHandler resolveTypeHandler(Object parameter, JdbcType jdbcType) {
        TypeHandler<?> handler;
        // 如果 parameter 为空，则返回 ObjectTypeHandler
        if (parameter == null) {
            handler = OBJECT_TYPE_HANDLER;
        }
        // 如果 parameter 不为空，则从 typeHandlerRegistry 处理器中获取 parameter参数类型 所对应的类型转换器
        else {
            handler = typeHandlerRegistry.getTypeHandler(parameter.getClass(), jdbcType);
            if (handler == null || handler instanceof UnknownTypeHandler) {
                handler = OBJECT_TYPE_HANDLER;
            }
        }
        return handler;
    }

    // 根据 columnName 从结果集中获取该列数据的类型处理器
    private TypeHandler resolveTypeHandler(ResultSet rs, String columnName) {
        try {
            // 列名 ---- 列索引 映射map
            Map<String, Integer> columnIndexLookUp = new HashMap<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i < rsmd.getColumnCount(); i++) {
                columnIndexLookUp.put(rsmd.getColumnName(i), i);
            }
            Integer columnIndex = columnIndexLookUp.get(columnName);
            TypeHandler<?> handler = null;
            if (columnIndex != null) {
                // 从 rsmd 中获取该 列索引 对应的数据转换器
                handler = resolveTypeHandler(rsmd, columnIndex);
            }
            if (handler == null || handler instanceof UnknownTypeHandler) {
                handler = OBJECT_TYPE_HANDLER;
            }
            return handler;
        } catch (SQLException e) {
            throw new TypeException("无法获取列名为 " + columnName + " 的 JDBC 类型！！！");
        }
    }

    private TypeHandler resolveTypeHandler(ResultSetMetaData rsmd, Integer columnIndex) {
        TypeHandler<?> handler = null;
        // 获得 jdbc type
        JdbcType jdbcType = safeGetJdbcTypeForColumn(rsmd, columnIndex);
        // 获得 java type
        Class<?> javaType = safeGetClassForColumn(rsmd, columnIndex);
        if (jdbcType != null && javaType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
        } else if (jdbcType != null) {
            handler = typeHandlerRegistry.getTypeHandler(jdbcType);
        } else if (javaType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType);
        }
        return handler;
    }

    private Class<?> safeGetClassForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
        try {
            return Resources.classForName(rsmd.getColumnClassName(columnIndex));
        } catch (Exception e) {
            return null;
        }
    }

    private JdbcType safeGetJdbcTypeForColumn(ResultSetMetaData rsmd, Integer columnIndex) {
        try {
            return JdbcType.forCode(rsmd.getColumnType(columnIndex));
        } catch (Exception e) {
            return null;
        }
    }
}
