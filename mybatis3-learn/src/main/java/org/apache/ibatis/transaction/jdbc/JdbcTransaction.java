package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @description 基于JDBC事务实现类
 * @author mrjimmylin
 * @date 2019/9/23 16:37
 */
public class JdbcTransaction implements Transaction {

    private static final Log log = LogFactory.getLog(JdbcTransaction.class);

    // 连接
    protected Connection connection;
    // 数据源
    protected DataSource dataSource;
    // 事务隔离级别
    protected TransactionIsolationLevel level;
    // 是否自动提交
    protected boolean autoCommit;

    public JdbcTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        this.dataSource = dataSource;
        this.level = level;
        this.autoCommit = autoCommit;
    }

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    // 获得 连接
    @Override
    public Connection getConnection() throws SQLException {
        // 如果 连接 为null，则 创建连接
        if (connection == null) {
            openConnection();
        }
        return connection;
    }

    private void openConnection() throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("打开 JDBC 连接！！");
        }
        // 从 数据源 中获取 连接
        connection = dataSource.getConnection();
        // 设置 事务隔离等级
        if (connection == null) {
            connection.setTransactionIsolation(level.getLevel());
        }
        // 设置 是否自动提交
        setDesiredAutocommit(autoCommit);
    }

    private void setDesiredAutocommit(boolean autoCommit) {

    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() throws SQLException {

    }

    @Override
    public Integer getTimeout() throws SQLException {
        return null;
    }
}
