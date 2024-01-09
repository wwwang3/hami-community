package top.wang3.hami.test;


import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class ClassTest {

    interface Handler<T> {
        void handle(T t);
    }

    static abstract class CommonHandler<T> implements Handler<T> {

        @Override
        public void handle(T t) {
            System.out.println(t);
        }
    }

    static abstract class MultiTypeHandler<V, T> implements Handler<T> {

        @Override
        public void handle(T t) {
            System.out.println(t);
        }

        abstract void doHandle(V v);
    }

    static class A extends CommonHandler<Integer> {

        @Override
        public void handle(Integer integer) {
            System.out.println("A handle integer");
        }
    }

    static class B implements Handler<String> {

        @Override
        public void handle(String t) {
            System.out.println("B handle String");
        }
    }

    static class C extends MultiTypeHandler<String, Integer> {

        @Override
        void doHandle(String s) {
            System.out.println(s);
        }
    }


    @Test
    void testGenericParameterType() {
        Handler<?> a  = new A();
        System.out.println(getGenericParameterType(a));

        B b = new B();
        System.out.println(getGenericParameterType(b));

        C c = new C();
        System.out.println(getGenericParameterType(c));
    }

    private Class<?> getGenericParameterType(Handler<?> handler) {
        Class<? extends Handler> handlerClass = handler.getClass();
        // 获取handler实现的接口类型
        Type[] interfacesTypes = handlerClass.getGenericInterfaces();
        if (interfacesTypes.length == 0) {
            // 为空获取其直接超类的泛型参数, 返回第一个
            ParameterizedType type = (ParameterizedType) handlerClass.getGenericSuperclass();
            return (Class<?>) type.getActualTypeArguments()[0];
        }
        for (Type t : interfacesTypes) {
            Class clazz = (Class) ((ParameterizedType) t).getRawType();
            if (clazz.equals(Handler.class)) {
                return (Class<?>) ((ParameterizedType) t).getActualTypeArguments()[0];
            }
        }
        return null;
    }
}