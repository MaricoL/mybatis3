package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mrjimmylin
 * @description 基于最少使用的淘汰机制
 * @date 2019/9/27 15:31
 */
public class LruCache implements Cache {

    // 被装饰的 Cache
    private final Cache delegate;

    // 基于 LinkedHashMap 实现淘汰机制
    private Map<Object, Object> keyMap;

    // 最老的键，即将要被淘汰的
    private Object eldestKey;

    public LruCache(Cache delegate) {
        this.delegate = delegate;
        // 初始化 keyMap 对象
        setSize(1024);
    }


    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public void putObject(Object key, Object value) {
        delegate.putObject(key, value);
        // 循环 kekMap
        cycleKeyList(key);
    }


    @Override
    public Object getObject(Object key) {
        // 刷新 keymap 的访问顺序
        keyMap.get(key);
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object object) {
        return delegate.removeObject(object);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyMap.clear();
    }

    private void cycleKeyList(Object key) {
        keyMap.put(key, key);
        if (eldestKey != null) {
            delegate.removeObject(eldestKey);
            eldestKey = null;
        }
    }

    // 初始化 keyMap 对象
    private void setSize(final int size) {
        // LinkedHashMap 的一个构造函数，当 accessOrder 为 true 时就会按照访问顺序排序，
        // 即最近访问的排在最后，最早访问的排在最前
        keyMap = new LinkedHashMap<Object, Object>(size, .75f, true) {

            // LinkedHashMap 自带的 removeEldestEntry()删除最老元素的方法，默认返回 false，即 不删除最老的元素
            // 这里需要重写该方法，在 一定条件下 找出最老的元素，返回 true，表示需要删除 最老元素
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                boolean tooBig = size() > size;
                if (tooBig) {
                    eldestKey = eldest.getKey();
                }
                return tooBig;
            }
        };
    }
}
