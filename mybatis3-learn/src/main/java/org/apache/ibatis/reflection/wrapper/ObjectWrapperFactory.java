package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;

public interface ObjectWrapperFactory {

    // 是否包装了 Object 对象
    boolean hasWrappereFor(Object object);

    // 获得指定对象的 ObjectMapper 对象
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
