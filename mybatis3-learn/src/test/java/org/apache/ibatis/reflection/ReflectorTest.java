package org.apache.ibatis.reflection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReflectorTest {

    @Test
    public void Test1() {
        Reflector reflector = new Reflector(Section.class);
        assertNotNull(reflector.getDefaultConstructor());
    }

    @Test
    public void Test2() {
        Reflector reflector = new Reflector(Section.class);
    }

    interface Entity<T>{
        T getId();

        void setId(T id);
    }
    static abstract class AbstractEntity implements Entity<Long>{
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

    }

    static class Section extends AbstractEntity implements Entity<Long>{
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setId(Long id , Long id2) {
            this.id = 2L;
        }

    }
}
