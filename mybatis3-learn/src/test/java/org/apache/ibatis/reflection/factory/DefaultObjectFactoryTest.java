package org.apache.ibatis.reflection.factory;

import org.apache.ibatis.reflection.ReflectionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultObjectFactoryTest {

    @Test
    public void Test1() {
        DefaultObjectFactory factory = new DefaultObjectFactory();
        TestClass testClass = factory.create(TestClass.class, Arrays.asList(String.class, Integer.class), Arrays.asList("jimmylin", 20));
        assertEquals("jimmylin", testClass.getUsername(), "username 的值不是 jimmylin");
        assertEquals(20, testClass.getAge(), "age 的值不是 20");
    }

    @Test
    public void createClassThrowErrorMsg() {
        DefaultObjectFactory factory = new DefaultObjectFactory();
        try {
            factory.create(TestClass.class, Collections.singletonList(String.class), Collections.singletonList("jimmylin"));
            fail("应该会抛出 ReflectionException!!!");
        } catch (Exception e) {
            assertTrue(e instanceof ReflectionException, "应该会抛出 ReflectionException 异常 !");
        }
    }

    @Test
    public void createHasMap() {
        DefaultObjectFactory factory = new DefaultObjectFactory();
        Map map = factory.create(Map.class, null, null);
        assertTrue(map instanceof HashMap, "创建的对象应该为 HashMap");
    }

    @Test
    public void createArrayList() {
        DefaultObjectFactory factory = new DefaultObjectFactory();
        List list = factory.create(List.class);
        assertTrue(list instanceof ArrayList, "创建的对象应该为 ArrayList");

        Collection collection = factory.create(Collection.class);
        assertTrue(collection instanceof ArrayList, "创建的对象应该为 ArrayList");

        Iterable iterable = factory.create(Iterable.class);
        assertTrue(iterable instanceof ArrayList, "创建的对象应该为 ArrayList");
    }


    @Test
    public void createTreeSet(){
        DefaultObjectFactory factory = new DefaultObjectFactory();
        SortedSet sortedSet = factory.create(SortedSet.class);
        assertTrue(sortedSet instanceof TreeSet, "创建的对象应该为 Arrayist");
    }


    @Test
    public void createHashSet(){
        DefaultObjectFactory factory = new DefaultObjectFactory();
        Set set = factory.create(Set.class);
        assertTrue(set instanceof HashSet, "创建的对象应该为 HashSet");
    }


}
