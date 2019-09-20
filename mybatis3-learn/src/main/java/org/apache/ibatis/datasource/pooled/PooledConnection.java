package org.apache.ibatis.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * @author mrjimmylin
 * @description 池化连接
 * @date 2019/9/20 16:22
 */
public class PooledConnection implements InvocationHandler {

    // 需要代理的接口的 Class 对象
    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

    // 连接 对象的标识
    private final int hashCode;
    // 真正的 连接 对象
    private final Connection realConnection;
    // 代理的 连接 对象
    private final Connection proxyConnection;
    // 所属的 dataSource 对象
    private final PooledDataSource dataSource;
    // 连接 创建时间
    private long createdTimestamp;
    // 连接 最后更新时间
    private long lastUsedTimestamp;
    // 连接 是否有效
    private boolean isValid;

    // 连接类型编码
    private int connectionTypeCode;


    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.hashCode = connection.hashCode();
        this.realConnection = connection;
        this.dataSource = dataSource;
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.isValid = true;
        // 创建代理的 Connection 对象
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    // 将 连接 设置为 无效
    public void invalidate() {
        this.isValid = false;
    }

    // 判断 连接 是否有效：
    // 连接 标识符 是否有效，真实连接 是否不为空，此连接是否连接正常
    public boolean isValid(){
        return isValid && realConnection != null && dataSource.pingConnection(this);
    }

    public Connection getRealConnection() {
        return realConnection;
    }

    public Connection getProxyConnection() {
        return proxyConnection;
    }

    // 获得 真是连接 的 哈希值
    public int getRealHashCode() {
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    // 获取 该连接 上次使用后所经过的时长
    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }


    // TODO: getAge()



    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        return null;
    }
}
