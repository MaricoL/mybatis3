package org.apache.ibatis.reflection.property;

import java.util.Iterator;

public class PropertyTokenizer implements Iterator<PropertyTokenizer> {

    // 当前字符串
    private String name;
    // 首次初始化 name 的值，因为如果有 index 的话 name 会被从新赋值
    private final String indexName;
    // 编号
    // 对于数组 name[0] ，则 index = 0
    // 对于 Map map[key] ，则 index = key
    private String index;
    // 剩余字符串
    private final String children;

    public PropertyTokenizer(String fullName) {
        // 1. 初始化 name 和 children
        int delim = fullName.indexOf(".");
        if (delim > -1) {
            name = fullName.substring(0, delim);
            children = fullName.substring(delim + 1);
        } else {
            name = fullName;
            children = null;
        }

        // 2. 记录当前 name
        indexName = name;

        // 3. 初始化 index，再次计算 name
        delim = name.indexOf('[');
        if (delim > -1) {
            index = name.substring(delim + 1, name.length() - 1);
            name = name.substring(0, delim);
        }
    }

    public String getName() {
        return name;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getIndex() {
        return index;
    }

    public String getChildren() {
        return children;
    }

    @Override
    public boolean hasNext() {
        return children != null;
    }

    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("不支持删除操作，因为没有意义！");
    }
}
