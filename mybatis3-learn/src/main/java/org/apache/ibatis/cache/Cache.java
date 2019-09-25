package org.apache.ibatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author mrjimmylin
 * @description 缓存容器 接口  SPI 服务提供者接口
 * @date 2019/9/25 14:54
 */
public interface Cache {

    // 获得该缓存容器标识
    String getId();

    // 添加指定的键值
    void putObject(Object key, Object value);

    // 获得指定键的值
    Object getObject(Object key);

    // 移除指定键的值
    Object removeObject(Object object);

    // 清空缓存
    void clear();

    // 获得容器中缓存的数量
    int getSize();

    // 3.2.6后不再调用
    default ReadWriteLock getReadWriteLock() {
        return null;
    }


}
