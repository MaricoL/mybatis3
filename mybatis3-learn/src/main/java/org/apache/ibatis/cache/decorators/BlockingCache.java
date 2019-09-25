package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mrjimmylin
 * @description 阻塞缓存
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
        // 获得基于当前 键 所对应的 锁
        acquireLock(key);
        // 获得 缓存值
        Object value = delegate.getObject(key);
        if (value != null) {
            // 如果 缓存值 不为空，则释放 锁
            releaseLock(key);
        }
        return value;
    }


    @Override
    public void putObject(Object key, Object value) {
        // 添加缓存键值对
        delegate.putObject(key, value);
        // 释放锁
        releaseLock(key);
    }


    @Override
    public Object removeObject(Object key) {
        // 释放锁
        releaseLock(key);
        return null;
    }

    private void acquireLock(Object key) {
        // 获取 锁
        Lock lock = getLockForKey(key);
        // 如果设置了 获取锁的超时时间
        if (timeout > 0) {
            // 尝试在 timeout 时间内获取锁，如果没有获取到，也不会一直等待获取，返回 false
            try {
                if (!lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    throw new CacheException("无法在 " + timeout + " 毫秒内在缓存标志为 " + delegate.getId() + " 的缓存中获取 键：" + key +
                            " 所对应的锁！");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            lock.lock();
        }
    }

    private void releaseLock(Object key) {
        ReentrantLock lock = locks.get(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    // 获得 指定键 的 对应锁
    private Lock getLockForKey(Object key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
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
