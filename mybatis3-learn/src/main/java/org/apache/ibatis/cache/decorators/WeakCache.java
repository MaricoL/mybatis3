    package org.apache.ibatis.cache.decorators;

    import org.apache.ibatis.cache.Cache;

    import java.lang.ref.ReferenceQueue;
    import java.lang.ref.WeakReference;
    import java.util.Deque;
    import java.util.LinkedList;

    /**
     * @author mrjimmylin
     * @description 基于弱引用的缓存
     * @date 2019/9/29 14:38
     */
    public class WeakCache implements Cache {
        // 强引用集合，避免对象被GC
        private final Deque<Object> hardLinkToAvoidGarbageCollection;
        // 当弱引用被GC后对象跟踪队列
        private final ReferenceQueue<Object> queueOfGarbageCollectionEntries;
        // 被装饰的 Cache 缓存
        private final Cache delegate;
        // 强引用的数量，即 hardLinkToAvoidGarbageCollection队列 的最大大小
        private int numberOfHardLinks;

        public WeakCache(Cache delegate) {
            this.delegate = delegate;
            this.numberOfHardLinks = 256;
            this.hardLinkToAvoidGarbageCollection = new LinkedList<>();
            this.queueOfGarbageCollectionEntries = new ReferenceQueue<>();
        }

        @Override
        public String getId() {
            return delegate.getId();
        }

        @Override
        public void putObject(Object key, Object value) {
            // 先删除被 GC 的对象
            removeGarbageCollectedItems();
            delegate.putObject(key, new WeakEntry(key, value, queueOfGarbageCollectionEntries));
        }


        @Override
        public Object getObject(Object key) {
            Object result = null;
            @SuppressWarnings("unchecked")
            WeakReference<Object> weakReference = (WeakReference<Object>) delegate.getObject(key);
            if (weakReference != null) {
                result = weakReference.get();
                // 如果result为空，则说明已经被GC回收掉了
                if (result == null) {
                    delegate.removeObject(key);
                }
                // 如果result非空，则说明还没有被GC回收掉，添加到 强引用 集合中，避免被GC掉
                else {
                    // ？？如果是相同的 key，可能存在重复添加的问题
                    hardLinkToAvoidGarbageCollection.addFirst(result);
                    // 如果 强引用 集合已满，则删除集合中最后一个元素
                    if (hardLinkToAvoidGarbageCollection.size() > numberOfHardLinks) {
                        hardLinkToAvoidGarbageCollection.removeLast();
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
            // 清空 hardLinkToAvoidGarbageCollection
            hardLinkToAvoidGarbageCollection.clear();
            // 移除已经被 GC 回收的对象
            removeGarbageCollectedItems();
            // 清空 delegate
            delegate.clear();

        }

        @Override
        public int getSize() {
            removeGarbageCollectedItems();
            return delegate.getSize();
        }


        // 移除已经被 GC 回收的对象
        private void removeGarbageCollectedItems() {
            WeakEntry weakEntry;
            while ((weakEntry = (WeakEntry) queueOfGarbageCollectionEntries.poll()) != null) {
                delegate.removeObject(weakEntry.key);
            }
        }

        // 弱引用对象，增加 key 字段
        private static class WeakEntry extends WeakReference<Object> {
            private final Object key;
            private WeakEntry(Object key, Object referent, ReferenceQueue<? super Object> q) {
                super(referent, q);
                this.key = key;
            }
        }

    }
