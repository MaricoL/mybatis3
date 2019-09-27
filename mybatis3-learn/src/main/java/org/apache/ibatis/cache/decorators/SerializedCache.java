package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.io.Resources;

import java.io.*;

/**
 * @author mrjimmylin
 * @description 支持序列化值的缓存
 * @date 2019/9/27 13:06
 */
public class SerializedCache implements Cache {

    // 被装饰的 Cache
    private final Cache delegate;

    public SerializedCache(Cache delegate) {
        this.delegate = delegate;
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
        if (value == null || value instanceof Serializable) {
            delegate.putObject(key, serialize((Serializable) value));
        }
    }

    @Override
    public Object getObject(Object key) {
        Object object = delegate.getObject(key);
        return object == null ? null : deserialize((byte[]) object);
    }


    private byte[] serialize(Serializable value) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(value);
            oos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new CacheException("序列化对象错误！！原因为：" + e.getCause());
        }
    }

    private Object deserialize(byte[] value) {
        Serializable result;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(value);
             ObjectInputStream ois = new CustomObjectInputStream(bis)) {
            result = (Serializable) ois.readObject();
        } catch (Exception e) {
            throw new CacheException("反序列化对象错误！！原因为：" + e.getCause());
        }
        return result;
    }

    public static class CustomObjectInputStream extends ObjectInputStream {

        public CustomObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
            return Resources.classForName(desc.getName());
        }
    }


    // 解析类
    @Override
    public Object removeObject(Object object) {
        return delegate.removeObject(object);
    }

    @Override
    public void clear() {
        delegate.clear();
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
