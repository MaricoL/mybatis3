package org.apache.ibatis.datasource.pooled;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mrjimmylin
 * @description 连接池状态  记录 空闲和活跃的{@link PooledConnection} 集合 以及 相关数据的统计
 * @date 2019/9/20 15:48
 */
public class PoolState {

    // 所属的 数据源 对象
    private PooledDataSource dataSource;

    // 空闲连接 集合
    protected final List<PooledConnection> idleConnections = new ArrayList<>();

    // 活跃连接 集合
    protected final List<PooledConnection> activeConnections = new ArrayList<>();

    // 统计 —— 获取连接的次数
    protected long requestCount = 0;

    // 统计 —— 获取连接的时间
    protected long accumulatedRequestTime = 0;

    // 统计 —— 获取到连接（非超时 + 超时）的时间
    protected long accumulatedCheckoutTime = 0;

    // 统计 —— 获取连接超时的次数
    protected long claimedOverdueConnectionCount = 0;

    // 统计 —— 获取连接超时的占用时长
    protected long accumulatedCheckoutTimeOfOverdueConnections = 0;

    // 统计 —— 等待连接的时间
    protected long accumulatedWaitTime = 0;

    // 统计 —— 等待连接的次数
    protected long hadToWaitCount = 0;

    // 统计 —— 获取到坏连接的次数
    protected long badConnectionCount = 0;


    public PoolState(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }


    // ====================== getter方法 ==========================================================

    public synchronized long getRequestCount() {
        return requestCount;
    }

    public synchronized long getAverageRequestTime() {
        return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
    }

    public synchronized long getAverageWaitTime() {
        return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
    }

    public synchronized long getHadToWaitCount() {
        return hadToWaitCount;
    }

    public synchronized long getBadConnectionCount() {
        return badConnectionCount;
    }

    public synchronized long getClaimedOverdueConnectionCount() {
        return claimedOverdueConnectionCount;
    }

    public synchronized long getAverageOverdueCheckoutTime() {
        return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
    }

    public synchronized long getAverageCheckoutTime() {
        return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
    }


    public synchronized int getIdleConnectionCount() {
        return idleConnections.size();
    }

    public synchronized int getActiveConnectionCount() {
        return activeConnections.size();
    }

    @Override
    public synchronized String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n===CONFINGURATION==============================================");
        builder.append("\n jdbcDriver                     ").append(dataSource.getDriver());
        builder.append("\n jdbcUrl                        ").append(dataSource.getUrl());
        builder.append("\n jdbcUsername                   ").append(dataSource.getUsername());
        builder.append("\n jdbcPassword                   ").append(dataSource.getPassword() == null ? "NULL" : "************");
        builder.append("\n poolMaxActiveConnections       ").append(dataSource.poolMaximumActiveConnections);
        builder.append("\n poolMaxIdleConnections         ").append(dataSource.poolMaximumIdleConnections);
        builder.append("\n poolMaxCheckoutTime            ").append(dataSource.poolMaximumCheckoutTime);
        builder.append("\n poolTimeToWait                 ").append(dataSource.poolTimeToWait);
        builder.append("\n poolPingEnabled                ").append(dataSource.poolPingEnabled);
        builder.append("\n poolPingQuery                  ").append(dataSource.poolPingQuery);
        builder.append("\n poolPingConnectionsNotUsedFor  ").append(dataSource.poolPingConnectionsNotUsedFor);
        builder.append("\n ---STATUS-----------------------------------------------------");
        builder.append("\n activeConnections              ").append(getActiveConnectionCount());
        builder.append("\n idleConnections                ").append(getIdleConnectionCount());
        builder.append("\n requestCount                   ").append(getRequestCount());
        builder.append("\n averageRequestTime             ").append(getAverageRequestTime());
        builder.append("\n averageCheckoutTime            ").append(getAverageCheckoutTime());
        builder.append("\n claimedOverdue                 ").append(getClaimedOverdueConnectionCount());
        builder.append("\n averageOverdueCheckoutTime     ").append(getAverageOverdueCheckoutTime());
        builder.append("\n hadToWait                      ").append(getHadToWaitCount());
        builder.append("\n averageWaitTime                ").append(getAverageWaitTime());
        builder.append("\n badConnectionCount             ").append(getBadConnectionCount());
        builder.append("\n===============================================================");
        return builder.toString();
    }
}
