import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * jdk 提供了一个unsafe类来直接通过 内存地址来操作变量
 * 该例子演示了 另外一种变换 变量值的方式
 *
 * @author liukunyang
 * @version V1.0
 * @Package concurrentTest
 * @date 2013-12-11 下午02:36:23
 */
public class UnsafeTest {

    private int i;

    /**
     * @param args
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @author liukunyang
     * @date 2013-3-20
     */

    public static void main(String[] args) throws SecurityException, NoSuchFieldException {

        // TODO Auto-generated method stub

        UnsafeTest ust = new UnsafeTest();

        /**


         * 抛unsafe security异常，所以通过反射调用


         * unsafe直接访问内存，所以不应在不受信用的类中访问


         * public static Unsafe getUnsafe() {
         * Class cc = sun.reflect.Reflection.getCallerClass(2);
         *


         if (cc.getClassLoader() != null)


         * throw new SecurityException("Unsafe");
         * return theUnsafe;
         * }


         *


         */
        //

        //        Unsafe unsafe = Unsafe.getUnsafe();

        Unsafe unsafe = null;

        try {

            Class<?> clazz = Unsafe.class;

            Field f;
            f = clazz.getDeclaredField("theUnsafe");

            f.setAccessible(true);

            unsafe = (Unsafe)f.get(clazz);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {

            e.printStackTrace();

        } catch (NoSuchFieldException e) {

            e.printStackTrace();

        }

        Long offfset = unsafe.objectFieldOffset(UnsafeTest.class.getDeclaredField("i"));

        unsafe.putInt(ust, offfset, 2);//修改掉i变量的值。

        System.out.println(ust.getI());

    }

    public int getI() {

        return i;

    }

    public void setI(int i) {

        this.i = i;

    }

}
