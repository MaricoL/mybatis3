package org.apache.ibatis.datasource.jndi;

import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.DataSourceFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * @author mrjimmylin
 * @description 基于JNDI的数据源工厂
 * @date 2019/9/18 14:00
 */
public class JndiDataSourceFactory implements DataSourceFactory {

    private static final String INITIAL_CONTEXT = "initial_context";
    public static final String DATA_SOURCE = "data_source";
    private static final String ENV_PREFIX = "env.";

    private DataSource dataSource;

    @Override
    public void setProperties(Properties props) {
        try {
            InitialContext initialContext;
            // 获得所有以 env. 开头的属性名---属性值
            Properties envProperties = getEnvProperties(props);
            if (envProperties == null) {
                initialContext = new InitialContext();
            } else {
                initialContext = new InitialContext(envProperties);
            }

            // 从 initialContext 中获取 DataSource 对象
            if (props.containsKey(INITIAL_CONTEXT) && props.containsKey(DATA_SOURCE)) {
                Context context = (Context) initialContext.lookup(props.getProperty(INITIAL_CONTEXT));
                dataSource = (DataSource) context.lookup(props.getProperty(DATA_SOURCE));
            } else if (props.containsKey(DATA_SOURCE)) {
                dataSource = (DataSource) initialContext.lookup(props.getProperty(DATA_SOURCE));
            }
        } catch (NamingException e) {
            throw new DataSourceException();
        }
    }


    @Override
    public DataSource getDataSource() {
        return this.dataSource;
    }


    // 从 props 中获得所有以 env. 开头的 属性名----属性值
    private static Properties getEnvProperties(Properties props) {
        Properties contextProperties = null;
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (name.startsWith(ENV_PREFIX)) {
                if (contextProperties == null) {
                    contextProperties = new Properties();
                }
                contextProperties.put(name.substring(ENV_PREFIX.length()), value);
            }
        }
        return contextProperties;
    }
}
