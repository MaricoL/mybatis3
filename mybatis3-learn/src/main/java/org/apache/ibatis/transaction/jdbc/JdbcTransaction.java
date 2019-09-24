package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author mrjimmylin
 * @description 基于JDBC事务实现类
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
        if (connection != null) {
            connection.setTransactionIsolation(level.getLevel());
        }
        // 设置 是否自动提交
        setDesiredAutocommit(autoCommit);
    }

    // 设置 自动提交
    private void setDesiredAutocommit(boolean autoCommit) {
        try {
            if (connection.getAutoCommit() != autoCommit) {
                if (log.isDebugEnabled()) {
                    log.debug("将连接 " + connection + " 设置为自动提交!!");
                }
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new TransactionException("无法设置自动提交参数，您的驱动可能不支持 setAutoCommit() 方法！" +
                    "原因：" + e.getCause());
        }
    }

    // 关闭 连接
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            // 将 自动提交 设置为 true
            resetAutoCommit();
            if (log.isDebugEnabled()) {
                log.debug("关闭连接 " + connection);
            }
            connection.close();
        }
    }

    // 从新设置 自动提交
    private void resetAutoCommit() {
        try {
            if (!connection.getAutoCommit()) {
                if (log.isDebugEnabled()) {
                    log.debug("将 连接：" + connection + " 的自动提交重新设置为 true！");
                }
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("在 关闭连接前，无法将 自动提交 设置为 true。原因：" + e.getCause());
            }
        }
    }

    @Override
    public void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("连接 即将提交！");
            }
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            if (log.isDebugEnabled()) {
                log.debug("连接 即将回滚！");
            }
            connection.rollback();
        }
    }

    @Override
    public Integer getTimeout() throws SQLException {
        return null;
    }
}
