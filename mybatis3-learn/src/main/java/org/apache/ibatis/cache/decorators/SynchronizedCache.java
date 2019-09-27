package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

/**
 * @description 同步的缓存
 * @author mrjimmylin
 * @date 2019/9/27 13:01
 */
public class SynchronizedCache implements Cache {
    // 装饰的 Cache 对象
    private final Cache delegate;

    public SynchronizedCache(Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public synchronized void putObject(Object key, Object value) {
        delegate.putObject(key, value);
    }

    @Override
    public synchronized Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public synchronized Object removeObject(Object object) {
        return delegate.removeObject(object);
    }

    @Override
    public synchronized void clear() {
        delegate.clear();
    }

    @Override
    public synchronized int getSize() {
        return delegate.getSize();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }
}
