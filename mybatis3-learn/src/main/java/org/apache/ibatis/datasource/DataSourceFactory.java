package org.apache.ibatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @description 数据源工厂
 * @author mrjimmylin
 * @date 2019/9/18 9:56
 */
public interface DataSourceFactory {

    // 设置 DataSource 配置
    void setProperties(Properties props);

    // 获得 DataSource 对象
    DataSource getDataSource();
}
