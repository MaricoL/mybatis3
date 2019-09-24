package org.apache.ibatis.transaction;

import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * @description 事务工厂 接口
 * @author mrjimmylin
 * @date 2019/9/24 14:13
 */
public interface TransactionFactory {
    // 设置工厂的属性
    default  void setProperties(Properties properties){

    }

    // 创建事务
    Transaction newTransaction(Connection connection);

    // 创建事务
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);
}
