package org.apache.ibatis.datasource.pooled;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
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
                            // 将 longestCheckoutTime 累加到 accumulatedCheckoutTimeOfOverdueConnections 进行统计 所有国企链接的检出时间
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
                                // 等待，直到超时，或者 popConnection 中归还连接时 唤醒
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


    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
