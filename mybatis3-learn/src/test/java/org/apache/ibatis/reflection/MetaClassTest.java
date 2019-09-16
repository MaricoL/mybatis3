package org.apache.ibatis.reflection;

import org.apache.ibatis.domain.misc.RichType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetaClassTest {

    @Test
    void shouldCheckGetterExistance() {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        assertTrue(meta.hasGetter("richField"));
        assertTrue(meta.hasGetter("richProperty"));
        assertTrue(meta.hasGetter("richList"));
        assertTrue(meta.hasGetter("richMap"));
        assertTrue(meta.hasGetter("richList[0].richField"));

        assertTrue(meta.hasGetter("richType"));
        assertTrue(meta.hasGetter("richType.richField"));
        assertTrue(meta.hasGetter("richType.richProperty"));
        assertTrue(meta.hasGetter("richType.richList"));
        assertTrue(meta.hasGetter("richType.richMap"));
        assertTrue(meta.hasGetter("richType.richList[0]"));

        assertEquals("richType.richProperty", meta.findProperty("richType.richProperty", false));

        assertFalse(meta.hasGetter("[0]"));
    }

    @Test
    void shouldCheckTypeForEachGetter() {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        assertEquals(String.class, meta.getGetterType("richField"));
        assertEquals(String.class, meta.getGetterType("richProperty"));
        assertEquals(List.class, meta.getGetterType("richList"));
        assertEquals(Map.class, meta.getGetterType("richMap"));
        // RichType 与源码测试类不一样，有做修改
        assertEquals(RichType.class, meta.getGetterType("richList[0]"));

        assertEquals(RichType.class, meta.getGetterType("richType"));
        assertEquals(String.class, meta.getGetterType("richType.richField"));
        assertEquals(String.class, meta.getGetterType("richType.richProperty"));
        assertEquals(List.class, meta.getGetterType("richType.richList"));
        assertEquals(Map.class, meta.getGetterType("richType.richMap"));
        assertEquals(RichType.class, meta.getGetterType("richType.richList[0]"));
    }


    @Test
    void shouldCheckTypeForEachSetter() {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass meta = MetaClass.forClass(RichType.class, reflectorFactory);
        assertEquals(String.class, meta.getSetterType("richField"));
        assertEquals(String.class, meta.getSetterType("richProperty"));
        assertEquals(List.class, meta.getSetterType("richList"));
        assertEquals(Map.class, meta.getSetterType("richMap"));
        assertEquals(List.class, meta.getSetterType("richList[0]"));

        assertEquals(RichType.class, meta.getSetterType("richType"));
        assertEquals(String.class, meta.getSetterType("richType.richField"));
        assertEquals(String.class, meta.getSetterType("richType.richProperty"));
        assertEquals(List.class, meta.getSetterType("richType.richList"));
        assertEquals(Map.class, meta.getSetterType("richType.richMap"));
        assertEquals(List.class, meta.getSetterType("richType.richList[0]"));
    }


}