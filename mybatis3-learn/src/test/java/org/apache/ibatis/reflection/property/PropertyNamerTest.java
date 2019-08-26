package org.apache.ibatis.reflection.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PropertyNamerTest {

    @Test
    public void Test1(){
        Assertions.assertEquals("name",PropertyNamer.methodToProperty("getName"));
        Assertions.assertEquals("name",PropertyNamer.methodToProperty("setName"));
        Assertions.assertEquals("open",PropertyNamer.methodToProperty("isOpen"));
    }
}
