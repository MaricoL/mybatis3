package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description 阻塞缓存
 * @author mrjimmylin
 * @date 2019/9/25 17:32
 */
public class BlockingCache implements Cache {
    // 阻塞等待超时时间
    private long timeout;
    // 装饰的 Cache 对象
    private Cache delegate;
    // 缓存 键 与 ReentrantLock对象 的 映射
    private ConcurrentHashMap<Object, ReentrantLock> locks;

    public BlockingCache(long timeout) {
        this.timeout = timeout;
        this.locks = new ConcurrentHashMap<>();
    }

    @Override
    public Object getObject(Object key) {
        return null;
    }

    @Override
    public void putObject(Object key, Object value) {

    }

    @Override
    public Object removeObject(Object object) {
        return null;
    }



    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
