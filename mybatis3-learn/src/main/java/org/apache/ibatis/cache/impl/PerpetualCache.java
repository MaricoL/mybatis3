package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mrjimmylin
 * @description 永不过期的缓存
 * @date 2019/9/25 15:06
 */
public class PerpetualCache implements Cache {

    // 标识
    private final String id;

    // 缓存容器
    private Map<Object, Object> cache = new HashMap<>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(Object object) {
        return cache.remove(object);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int getSize() {
        return cache.size();
    }

    // 根据 id 进行比较是否相等
    @Override
    public boolean equals(Object obj) {
        if (getId() == null) {
            throw new CacheException("缓存实例需要一个标识Id！");
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cache)) {
            return false;
        }
        Cache cache = (Cache) obj;
        return getId().equals(cache.getId());
    }

    // 返回 id 的哈希值
    @Override
    public int hashCode() {
        if (getId() == null) {
            throw new CacheException("缓存实例需要一个标识Id！");
        }
        return getId().hashCode();
    }
}
