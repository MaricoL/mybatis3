package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

/**
 * @description 定时清空整个容器的缓存
 * @author mrjimmylin
 * @date 2019/9/27 14:54
 */
public class ScheduledCache implements Cache {
    // 被装饰的 Cache
    private final Cache delegate;

    // 清空间隔，单位：毫秒
    protected long clearInterval;

    // 最后清空时间
    protected long lastClear;

    public ScheduledCache(Cache delegate) {
        this.delegate = delegate;
        this.clearInterval = 60 * 60 * 1000; // 1小时
        this.clearInterval = System.currentTimeMillis();
    }

    public void setClearInterval(long clearInterval) {
        this.clearInterval = clearInterval;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        // 判断是否全部清空
        clearWhenStale();
        delegate.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return clearWhenStale() ? null : delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object object) {
        clearWhenStale();
        return delegate.removeObject(object);
    }

    @Override
    public void clear() {
        lastClear = System.currentTimeMillis();
        delegate.clear();
    }

    @Override
    public int getSize() {
        // 判断是否全部清空
        clearWhenStale();
        return delegate.getSize();
    }

    private boolean clearWhenStale() {
        if (System.currentTimeMillis() - lastClear > clearInterval) {
            clear();
            return true;
        }
        return false;
    }
}
