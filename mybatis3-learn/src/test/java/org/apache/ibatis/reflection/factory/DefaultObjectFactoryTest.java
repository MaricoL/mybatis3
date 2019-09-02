package org.apache.ibatis.reflection.factory;

import org.apache.ibatis.reflection.ReflectionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultObjectFactoryTest {

    @Test
    public void Test1() {
        DefaultObjectFactory factory = new DefaultObjectFactory();
        TestClass testClass = factory.create(TestClass.class, Arrays.asList(String.class, Integer.class), Arrays.asList("jimmylin", 20));
        assertEquals("jimmylin", testClass.getUsername(), "username 的值不是 jimmylin");
        assertEquals(20, testClass.getAge(), "age 的值不是 20");
    }

    @Test
    public void Test2(){
        DefaultObjectFactory factory = new DefaultObjectFactory();
        try {
            factory.create(TestClass.class, Collections.singletonList(String.class), Collections.singletonList("jimmylin"));
            Assertions.fail("应该会抛出 ReflectionException!!!");
        } catch (Exception e) {
            assertTrue(e instanceof ReflectionException, "应该会抛出 ReflectionException 异常 !");
        }

    }


}
