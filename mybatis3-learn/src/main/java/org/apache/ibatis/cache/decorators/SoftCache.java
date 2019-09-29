package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author mrjimmylin
 * @description 基于对象的软引用的缓存
 * @date 2019/9/29 16:06
 */
public class SoftCache implements Cache {

    private final Deque<Object> hardLinksToAvoidGarbageCollection;
    private final ReferenceQueue<Object> referenceQueue;
    private final Cache delegate;
    private int numberOfHardLinks;

    public SoftCache(Cache delegate) {
        this.delegate = delegate;
        hardLinksToAvoidGarbageCollection = new LinkedList<>();
        referenceQueue = new ReferenceQueue<>();
        numberOfHardLinks = 256;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        // 先删除已经被GC掉的对象
        removeGarbageCollectedItems();
        delegate.putObject(key, new SoftEntry(key, value, referenceQueue));
    }


    @Override
    public Object getObject(Object key) {
        Object result = null;
        @SuppressWarnings("unchecked")
        SoftReference<Object> softReference = (SoftReference<Object>) delegate.getObject(key);
        if (softReference != null) {
            result = softReference.get();
            // 如果 result 为空，则说明已经被GC掉了
            if (result == null) {
                result = softReference.get();
            }
            // 如果 result 非空，则说明还没有被回收掉，放入 强引用 集合中
            else {
                synchronized (hardLinksToAvoidGarbageCollection) {
                    hardLinksToAvoidGarbageCollection.addFirst(result);
                    if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
                        hardLinksToAvoidGarbageCollection.removeLast();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Object removeObject(Object object) {
        removeGarbageCollectedItems();
        return delegate.removeObject(object);
    }

    @Override
    public void clear() {
        synchronized (hardLinksToAvoidGarbageCollection) {
            hardLinksToAvoidGarbageCollection.clear();
        }
        removeGarbageCollectedItems();
        delegate.clear();
    }

    @Override
    public int getSize() {
        removeGarbageCollectedItems();
        return delegate.getSize();
    }

    // 删除已经被GC掉的对象
    private void removeGarbageCollectedItems() {
        SoftEntry softEntry;
        while ((softEntry = (SoftEntry) referenceQueue.poll()) != null) {
            delegate.removeObject(softEntry.key);
        }
    }

    private static class SoftEntry extends SoftReference<Object> {
        private final Object key;

        public SoftEntry(Object key, Object referent, ReferenceQueue<? super Object> q) {
            super(referent, q);
            this.key = key;
        }
    }

}
