package org.apache.ibatis.datasource.unpooled;

import org.apache.ibatis.io.Resources;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author mrjimmylin
 * @description 非池化数据源
 * @date 2019/9/18 14:53
 */
public class UnpooledDataSource implements DataSource {

    // 驱动的类加载器
    private ClassLoader driverClassLoader;
    // 驱动的属性
    private Properties driverProperties;
    // 已注册的驱动
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

    // 驱动名
    private String driver;
    // 数据库URL
    private String url;
    // 用户名
    private String username;
    // 密码
    private String password;

    // 事务自动提交
    private Boolean autoCommit;

    // 默认事务隔离级别
    private Integer defaultTransactionIsolationLevel;

    // 默认网络超时时间
    private Integer defaultNetworkTimeout;

    static {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    // 无参构造函数
    public UnpooledDataSource() {
    }

    public UnpooledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(String driver, String url, Properties driverProperties) {
        this.driver = driver;
        this.url = url;
        this.driverProperties = driverProperties;
    }

    public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
        this.driverClassLoader = driverClassLoader;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
        this.driverClassLoader = driverClassLoader;
        this.driver = driver;
        this.url = url;
        this.driverProperties = driverProperties;
    }


    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (driverProperties != null) {
            props.putAll(driverProperties);
        }
        if (username != null) {
            props.setProperty("username", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        return doGetConnection(props);


    }

    private Connection doGetConnection(Properties props) throws SQLException {
        // 1. 初始化驱动
        initialDriver();
        // 2. 获取 Connection 对象
        Connection connection = DriverManager.getConnection(url, props);
        // 3. 配置 Connection 对象
        configureConnection(connection);
        return connection;
    }


    // 初始化驱动
    private synchronized void initialDriver() throws SQLException {
        // 如果 registeredDrivers 中不包含 driver，则进行初始化
        if (!registeredDrivers.containsKey(driver)) {
            Class<?> driverType;

            try {
                if (driverClassLoader != null) {
                    driverType = Class.forName(driver, true, driverClassLoader);
                } else {
                    driverType = Resources.classForName(driver);
                }

                //使用默认构造函数创建 driver 对象
                Driver driverInstance = (Driver) driverType.getDeclaredConstructor().newInstance();
                // 注册到 DriverManager 中
                DriverManager.registerDriver(new DriverProxy(driverInstance));
                // 放入 registeredDrivers 中
                registeredDrivers.put(driver, driverInstance);
            } catch (Exception e) {
                throw new SQLException("无法在 UnPooledDataSrouce 数据源中设置属性！，原因是：" + e);
            }
        }
    }

    // 配置 Connection 对象
    private void configureConnection(Connection connection) throws SQLException {
        // 设置网络超时时间
        if (defaultNetworkTimeout != null) {
            connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), defaultNetworkTimeout);
        }
        // 设置自动提交事务
        if (autoCommit != null && autoCommit != connection.getAutoCommit()) {
            connection.setAutoCommit(autoCommit);
        }
        // 设置默认事务隔离级别
        if (defaultTransactionIsolationLevel != null) {
            connection.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
    }

    private static class DriverProxy implements Driver {
        private Driver driver;

        public DriverProxy(Driver driver) {
            this.driver = driver;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return this.driver.connect(url,info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return this.driver.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return this.driver.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return  this.driver.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() {
            return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + "没有一个包装类！");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public void setLoginTimeout(int loginTimeout) {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
