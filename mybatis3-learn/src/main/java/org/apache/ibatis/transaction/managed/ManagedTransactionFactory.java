package org.apache.ibatis.transaction.managed;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

public class ManagedTransactionFactory implements TransactionFactory {

    private boolean closeConnection = false;

    // 从 属性配置中 获取 closeConnection 属性
    @Override
    public void setProperties(Properties properties) {
        if (properties != null) {
            String closeConnection = properties.getProperty("closeConnection");
            if (closeConnection != null) {
                this.closeConnection = Boolean.parseBoolean(closeConnection);
            }
        }
    }

    @Override
    public Transaction newTransaction(Connection connection) {
        return new ManagedTransaction(connection, closeConnection);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new ManagedTransaction(dataSource, level, autoCommit);
    }
}
