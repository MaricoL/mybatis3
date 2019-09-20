package org.apache.ibatis.datasource.pooled;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author mrjimmylin
 * @description 池化数据源
 * @date 2019/9/19 13:19
 */
public class PooledDataSource implements DataSource {

    private static final Log log = LogFactory.getLog(PooledDataSource.class);

    // 连接池状态 对象
    private final PoolState poolState = new PoolState(this);

    // 非池化数据源 对象
    private final UnpooledDataSource dataSource;

    // --------------------可选配置属性--------------------

    // 最大活跃连接数
    protected int poolMaximumActiveConnections = 10;
    // 最大空闲连接数
    protected int poolMaximumIdleConnections = 5;
    // 最大可回收时间
    // 如果达到了最大活动连接数时，此时如果有程序要获取连接
    // 则先检查最先使用的连接，看其是否超出了该时间
    // 如果超出了此时间，则回收此连接
    protected int poolMaximumCheckoutTime = 20000;
    // 重新获取连接时间
    // 如果获取连接超过了该时间
    // 连接池会打印日志，并重新获取一个新的连接
    protected int poolTimeToWait = 20000;
    // 最大坏连接容忍度
    // 如果线程获取到的是一个坏连接
    // 那么数据源允许该线程重新获取一个新的连接
    // 但是这个重新尝试的次数不应该大于 最大空闲连接数 与 最大坏连接容忍度 之和
    protected int poolMaximumLocalBadConnectionTolerance = 3;
    // 侦测查询语句
    // 检查与数据库连接是否正常工作并准备接受请求
    protected String poolPingQuery = "NO PING QUERRY SET";
    // 是否开启侦测查询
    // 若要开启，需要设置 poolPingQuery 属性为一个可执行的SQL语句（最好是一个速度非常快的语句）
    // 默认值为 false
    protected boolean poolPingEnabled;
    // 配置 poolPingQuery 侦测频率
    // 默认值为 0，即：所有连接每一时刻都被侦测，当且仅当 poolPingEnabled 为 true 时才生效
    protected int poolPingConnectionsNotUsedFor;

    // -------------------------------------------------------------------------------------------------------

    // 期望 Connection 的类型编码
    private int expectedConnectionTypeCode;


    public PooledDataSource() {
        this.dataSource = new UnpooledDataSource();
    }

    public PooledDataSource(UnpooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PooledDataSource(String driver, String url, String username, String password) {
        this.dataSource = new UnpooledDataSource(driver, url, username, password);
        this.expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }

    public PooledDataSource(String driver, String url, Properties driverProperties) {
        this.dataSource = new UnpooledDataSource(driver, url, driverProperties);
        this.expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
        this.dataSource = new UnpooledDataSource(driverClassLoader, driver, url, username, password);
        this.expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }

    public PooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
        this.dataSource = new UnpooledDataSource(driverClassLoader, driver, url, driverProperties);
        this.expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
    }

    // 计算 Connection 类型编码
    private int assembleConnectionTypeCode(String url, String username, String password) {
        return (url + username + password).hashCode();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }


    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }


    // 获得一个池化连接对象
    private PooledConnection popConnection(String username, String password) throws SQLException {
        // 最终获得的池化连接对象
        PooledConnection conn = null;
        // 是否有要等待的连接
        boolean countedWait = false;
        // 获取本次连接开始时间
        long t = System.currentTimeMillis();
        // 记录坏连接的次数
        int localBadConnectionCount = 0;

        // 不停的获取连接对象
        while (conn == null) {
            synchronized (poolState) {
                // 如果 空闲连接 非空
                if (!poolState.idleConnections.isEmpty()) {
                    // 通过移除的方式获得连接
                    conn = poolState.idleConnections.remove(0);
                    // 打印日志
                    if (log.isDebugEnabled()) {
                        log.debug("从池中检出池化连接： " + conn.getRealHashCode());
                    }
                }
                // 如果 空闲连接 为空
                else {
                    // 则判断 正在活跃的连接数 是否小于 最大活跃连接数
                    // 如果小于，则创建一个新的 池化连接
                    if (poolState.activeConnections.size() < poolMaximumActiveConnections) {
                        conn = new PooledConnection(dataSource.getConnection(), this);
                        if (log.isDebugEnabled()) {
                            log.debug("创建了一个新的连接：" + conn.getRealHashCode() + " .");
                        }
                    }
                    // 否则，无法创建新的连接
                    else {
                        // 从活跃的连接中获取第一个连接
                        PooledConnection oldestActiveConnection = poolState.activeConnections.get(0);
                        // 获取该连接的检出时间
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                        // 如果该连接超时，则移除超时连接
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            // 将超时的连接数+1
                            poolState.claimedOverdueConnectionCount++;
                            // 将 longestCheckoutTime 累加到 accumulatedCheckoutTimeOfOverdueConnections 进行统计 所有超时连接的检出时间
                            poolState.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            // 将 longestCheckoutTime 累加到 accumulatedCheckoutTime 进行统计 所有连接的检出时间
                            poolState.accumulatedCheckoutTime += longestCheckoutTime;
                            // 将 该过期的连接 从 活跃的连接池 中移除
                            poolState.activeConnections.remove(oldestActiveConnection);
                            // 如果该连接不是 自动提交 的，则需要 回滚
                            if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                                try {
                                    oldestActiveConnection.getRealConnection().rollback();
                                } catch (SQLException e) {
                                    log.debug(oldestActiveConnection.getRealHashCode() + " 是一个坏连接，无法回滚！！！");
                                }
                            }

                            // 创建新的 PooledConnection 对象
                            conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                            // 设置 创建连接 时间戳
                            conn.setCreatedTimestamp(oldestActiveConnection.getCreatedTimestamp());
                            // 设置 最后一次使用 时间戳
                            conn.setLastUsedTimestamp(oldestActiveConnection.getLastUsedTimestamp());
                            // 设置 oldestActiveConnection 无效
                            oldestActiveConnection.invalidate();
                            if (log.isDebugEnabled()) {
                                log.debug("将超时的连接重新创建：" + oldestActiveConnection.getRealHashCode() + " .");
                            }
                        }
                        // 否则，进入阻塞等待
                        else {
                            try {
                                if (!countedWait) {
                                    // 计数+1
                                    poolState.hadToWaitCount++;
                                    countedWait = true;
                                }
                                if (log.isDebugEnabled()) {
                                    log.debug("等待连接长达 " + poolTimeToWait + "秒。");
                                }
                                long wt = System.currentTimeMillis();
                                // 等待，直到超时，或者从 pingConnection() 中归还连接时 唤醒
                                poolState.wait(poolTimeToWait);
                                // 统计等待的时间
                                poolState.accumulatedWaitTime += System.currentTimeMillis() - wt;
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
                // 获取到连接
                if (conn != null) {
                    // 通过 ping 来测试连接是否有效
                    if (conn.isValid()) {
                        // 如果非自动提交，又进行了以回滚
                        if (!conn.getRealConnection().getAutoCommit()) {
                            conn.getRealConnection().rollback();
                        }
                        // 设置获取连接的属性
                        conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        conn.setCheckoutTimestamp(System.currentTimeMillis());
                        conn.setLastUsedTimestamp(System.currentTimeMillis());
                        // 添加到 活跃的连接集合 中
                        poolState.activeConnections.add(conn);
                        // 对获取连接成功的进行统计
                        poolState.requestCount++;
                        // 累加 获取本次连接的总时长
                        poolState.accumulatedRequestTime += System.currentTimeMillis() - t;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("从池中返回了一个坏的连接！");
                        }
                        // 统计坏连接的数量
                        poolState.badConnectionCount++;
                        localBadConnectionCount++;
                        // 将 conn 置为空，可以继续获取连接
                        conn = null;
                        //
                        if (localBadConnectionCount > poolMaximumIdleConnections + poolMaximumLocalBadConnectionTolerance) {
                            if (log.isDebugEnabled()) {
                                log.debug("无法从数据源获得一个好的连接！");
                            }
                            throw new SQLException("无法从数据源获得一个好的连接！");
                        }
                    }
                }
            }
        }


        // 获取不到连接，抛出异常
        if (conn == null) {
            if (log.isDebugEnabled()) {
                log.debug("未知的严重错误情况。数据连接池返回一个 null 的连接！！");
            }
            throw new SQLException("未知的严重错误情况。数据连接池返回一个 null 的连接！！");
        }
        return conn;
    }

    // 归还 连接 到数据库连接池中
    protected void pushConnection(PooledConnection conn) throws SQLException {
        synchronized (poolState) {
            // 将 连接 从 活跃连接集合中 移除
            poolState.activeConnections.remove(conn);
            // 判断 该连接 是否有效
            if (conn.isValid()) {
                // 判断是否超过 空闲连接数 上限，并且 该连接 和 当前数据源表示信息 一致
                if (poolState.idleConnections.size() < poolMaximumIdleConnections && conn.getConnectionTypeCode() == expectedConnectionTypeCode) {
                    // 累加 连接 的使用时长
                    poolState.accumulatedCheckoutTime += conn.getCheckoutTime();
                    // 如果 连接 没有设置自动提交，则进行事务回滚
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    // 创建新的 PooledConnection 对象
                    PooledConnection newConn = new PooledConnection(conn.getRealConnection(), this);
                    // 放入 空闲连接集合 中
                    poolState.idleConnections.add(newConn);
                    // 设置 新连接 的 创建 时间戳
                    newConn.setCreatedTimestamp(conn.getCreatedTimestamp());
                    // 设置 新连接 的 最后一次使用 时间戳
                    newConn.setLastUsedTimestamp(conn.getLastUsedTimestamp());
                    // 将 原连接 设置为 无效
                    conn.invalidate();
                    if (log.isDebugEnabled()) {
                        log.debug("返回连接：" + newConn.getRealHashCode() + " 到数据库连接池中。");
                    }
                    // 唤醒 阻塞连接
                    poolState.notifyAll();
                }
                // 如果超过了 空闲连接数 上限
                else {
                    // 累加 连接 的使用时长
                    poolState.accumulatedCheckoutTime += conn.getCheckoutTime();
                    // 如果 连接 没有设置自动提交，则进行事务回滚
                    if (!conn.getRealConnection().getAutoCommit()) {
                        conn.getRealConnection().rollback();
                    }
                    // 关闭真正的数据库连接
                    conn.getRealConnection().close();
                    if (log.isDebugEnabled()) {
                        log.debug("关闭连接：" + conn.getRealHashCode());
                    }
                    // 将 连接 设置为 无效
                    conn.invalidate();
                }
            }
            // 如果该 连接 无效
            else {
                if (log.isDebugEnabled()) {
                    log.debug("一个坏的连接（" + conn.getRealHashCode() + "）企图返回到连接池中，已将其丢弃！");
                }
                poolState.badConnectionCount++;
            }
        }
    }

    // 向数据库发送 ping 侦测查询语句，来判断你数据库语句是否有效
    protected boolean pingConnection(PooledConnection conn) {
        // 是否 ping 成功
        boolean result;

        // 判断 该连接 是否已经关闭，如果已经关闭了，肯定 ping 失败
        try {
            result = !conn.getRealConnection().isClosed();
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("连接：" + conn.getRealHashCode() + "侦测查询失败！原因是：" + e);
            }
            result = false;
        }

        if (result) {
            // 是否启用 侦测查询
            if (poolPingEnabled) {
                // 判断是否长时间未使用，如果是，才发起 ping 查询
                if (poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("连接：" + conn.getRealHashCode() + " 正在发起 ping这侦测查询。。。。。");
                        }
                        // 获取 连接
                        Connection realConn = conn.getRealConnection();
                        // 执行 侦测查询
                        try (Statement statement = realConn.createStatement()) {
                            statement.executeQuery(poolPingQuery).close();
                        }
                        // 如果 连接 是 非自动提交，则进行回滚
                        if (!realConn.getAutoCommit()) {
                            realConn.rollback();
                        }
                        // 设置标识为 true，表示 侦测查询 成功
                        result = true;
                        if (log.isDebugEnabled()) {
                            log.warn("该连接：" + conn.getRealHashCode() + " 执行侦测查询成功，连接正常！");
                        }
                    } catch (Exception e) {
                        log.warn("连接 " + conn.getRealHashCode() + " 执行侦测查询失败！原因：" + e.getMessage());
                        // 关闭 真实连接
                        try {
                            conn.getRealConnection().close();
                        } catch (SQLException ex) {
                            // 忽略异常信息
                            // ex.printStackTrace();
                        }

                        // 设置标识为 false，表示 侦测查询 失败
                        result = false;
                        if (log.isDebugEnabled()) {
                            log.debug("该连接：" + conn.getRealHashCode() + " 不正常！原因：" + e.getMessage());
                        }
                    }
                }
            }
        }
        return result;
    }

    // 强制关闭所有 活跃连接集合(activeConnections) 和 空闲连接集合(idleConnections) 中所有的连接
    public void forceCloseAll() {
        synchronized (poolState) {
            // 计算 数据源标识信息
            expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
            // 遍历 活跃连接集合
            for (int i = poolState.activeConnections.size(); i > 0; i--) {
                try {
                    // 将 每个连接 从 集合 中移除
                    PooledConnection conn = poolState.activeConnections.remove(i - 1);
                    // 并将 连接 设置为 无效
                    conn.invalidate();

                    // 如果有事务未提交，则 回滚事务
                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                } catch (Exception e) {
                    // 忽略异常信息
                    // e.printStackTrace();
                }
            }

            for (int i = poolState.idleConnections.size(); i > 0; i--) {
                try {
                    // 将 每个连接 从 集合 中移除
                    PooledConnection conn = poolState.idleConnections.remove(i - 1);
                    // 并将 连接 设置为 无效
                    conn.invalidate();

                    // 如果有事务未提交，则 回滚事务
                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                } catch (Exception e) {
                    // 忽略异常信息
                    // e.printStackTrace();
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("池化数据源已经强制关闭所有的连接！");
        }
    }

    // 获得真正的 Connection 对象
    public static Connection unwrapConnection(Connection conn) {
        // 如果传入的时代理对象
        if (Proxy.isProxyClass(conn.getClass())) {
            // 获得 InvocationHandler 对象
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(conn);
            // 如果时 PooledConnection 类型
            if (invocationHandler instanceof PooledConnection) {
                return ((PooledConnection) invocationHandler).getRealConnection();
            }
        }
        return conn;
    }


    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " 不是一个包装类！");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    // 当 PooledDataSource对象 被释放时执行
    @Override
    protected void finalize() throws Throwable {
        // 关闭池中所有的连接
        forceCloseAll();
        super.finalize();
    }


    public String getDriver() {
        return dataSource.getDriver();
    }

    public String getUrl() {
        return dataSource.getUrl();
    }

    public String getUsername() {
        return dataSource.getUsername();
    }

    public String getPassword() {
        return dataSource.getPassword();
    }

    public boolean isAutoCommit() {
        return dataSource.isAutoCommit();
    }

    public Integer getDefaultTransactionIsolationLevel() {
        return dataSource.getDefaultTransactionIsolationLevel();
    }

    public Properties getDriverProperties() {
        return dataSource.getDriverProperties();
    }

    public Integer getDefaultNetworkTimeout() {
        return dataSource.getDefaultNetworkTimeout();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public int getPoolMaximumLocalBadConnectionTolerance() {
        return poolMaximumLocalBadConnectionTolerance;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }



    public PoolState getPoolState() {
        return poolState;
    }


}
