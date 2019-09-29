package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @description 基于先进先出的淘汰机制
 * @author mrjimmylin
 * @date 2019/9/27 15:06
 */
public class FifoCache implements Cache {

    // 装饰的 Cache
    private final Cache delegate;

    // 双端队列，记录缓存键的添加
    private final Deque<Object> keyList;

    // 队列上限
    private int size;

    public FifoCache(Cache delegate , Deque<Object> keyList, int size) {
        this.delegate = delegate;
        // 使用 LinkedList
        this.keyList = new LinkedList<>();
        this.size = 1024;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        // 循环 keyList，记录 key
        cycleKeyList(key);
        delegate.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object object) {
        // TODO: 在移除 key 的时候应该也要从 keyList 中移除 该key
        return delegate.removeObject(object);
    }

    @Override
    public void clear() {
        keyList.clear();
        delegate.clear();
    }

    // 循环 keyList
    private void cycleKeyList(Object key) {
        // 添加到尾部
        // TODO：这里应该要判断 keyList 是否已经有 该key了，
        //  否则如果添加两个相同的key，因为cache缓存（hashMap）只能存储一个相同的键，所以数量上不对应
        keyList.addLast(key);
        // 判断 keyList 是否已经达到 size 上限
        if (keyList.size() > size) {
            Object oldKey = keyList.removeFirst();
            delegate.removeObject(oldKey);
        }
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void setSize(int size) {
        this.size = size;
    }
}
