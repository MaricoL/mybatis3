package org.apache.ibatis.datasource;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @description 数据源异常
 * @author mrjimmylin
 * @date 2019/9/18 10:58
 */
public class DataSourceException extends PersistenceException
{
    public DataSourceException() {
        super();
    }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }
}
