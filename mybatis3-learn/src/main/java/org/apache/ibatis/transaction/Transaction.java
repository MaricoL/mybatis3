package org.apache.ibatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @description 事务 接口
 * @author mrjimmylin
 * @date 2019/9/23 16:33
 */
public interface Transaction {

    // 获取 连接
    Connection getConnection() throws SQLException;

    // 关闭 连接
    void close() throws SQLException;

    // 提交 事务
    void commit() throws SQLException;

    // 回滚 事务
    void rollback() throws SQLException;

    // 获得 事务超时时间
    Integer getTimeout() throws SQLException;

}
