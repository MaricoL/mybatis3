package org.apache.ibatis.datasource.unpooled;

import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author mrjimmylin
 * @description 非池化数据源工厂
 * @date 2019/9/18 11:04
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    private static final String DRIVER_PROPERTY_PREFIX = "driver.";
    private static final int DRIVER_PROPERTY_PREFIX_LENGTH = DRIVER_PROPERTY_PREFIX.length();

    // 数据源
    protected DataSource dataSource;

    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }

    @Override
    public void setProperties(Properties props) {
        // 用来存储以 driver. 开头的属性名---属性值
        Properties driverProperties = new Properties();
        // 创建 dataSource 的 MetaObject 对象
        MetaObject metaDataSource = SystemMetaObject.forObject(dataSource);
        // 遍历 props 集合
        for (Object key : props.keySet()) {
            String propertyName = (String) key;
            // 如果是以 driver. 开头的属性名
            if (propertyName.startsWith(DRIVER_PROPERTY_PREFIX)) {
                String value = props.getProperty(propertyName);
                driverProperties.setProperty(propertyName.substring(DRIVER_PROPERTY_PREFIX_LENGTH), value);
            }
            // 如果不是以 driver. 开头的，但是该属性名有 setter 方法的
            else if (metaDataSource.hasSetter(propertyName)) {
                String value = (String) props.get(propertyName);
                // 将 value 转换成合适的类型
                Object convertedValue = convertValue(metaDataSource, propertyName, value);
                // 进行字段赋值
                metaDataSource.setValue(propertyName, convertedValue);
            } else {
                throw new DataSourceException("未知的数据源配置！");
            }

            // 将 driverProperties 注入到 metaDataSource
            if (driverProperties.size() > 0) {
                metaDataSource.setValue("driverProperties", driverProperties);
            }
        }

    }

    // 将 value 转换成 合适的类型
    private Object convertValue(MetaObject metaDataSource, String propertyName, String value) {
        Object convertedValue = value;
        Class<?> setterType = metaDataSource.getSetterType(propertyName);
        if (setterType == Integer.class || setterType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (setterType == Long.class || setterType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (setterType == Boolean.class || setterType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        return convertedValue;
    }

    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }
}
