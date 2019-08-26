package org.apache.ibatis.reflection;

import org.junit.jupiter.api.Test;

public class ReflectorTest {

    @Test
    public void Test1() {
        Reflector reflector = new Reflector(Section.class);
        System.out.println(reflector.getDefaultConstructor());
    }


    static class Section {
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
