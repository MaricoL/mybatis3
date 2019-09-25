package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * @author mrjimmylin
 * @description 支持打印日志的缓存
 * @date 2019/9/25 16:04
 */
public class LoggingCache implements Cache {

    private final Log log;
    // 被装饰的 Cache 对象
    private final Cache delegate;
    protected int requests = 0;
    protected int hits = 0;

    public LoggingCache(Cache delegate) {
        this.delegate = delegate;
        this.log = LogFactory.getLog(getId());
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        delegate.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        // 请求次数自增
        requests++;
        // 获取缓存
        final Object value = delegate.getObject(key);
        if (value != null) {
            // 如果缓存命中，则 命中次数 自增
            hits++;
        }
        if (log.isDebugEnabled()) {
            log.debug("缓存[" + getId() + "]命中比例：" + getHitRatio());
        }
        return value;
    }

    // 计算缓存命中比例
    private double getHitRatio() {
        return (double) hits / (double) requests;
    }

    @Override
    public Object removeObject(Object object) {
        return delegate.removeObject(object);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
