import java.util.concurrent.*;

/**
 * Created by luairan on 2016/11/17.
 */
public class Ts {
//    public static void main(String[] args) {
//
//        boolean needSendAlarm  = true;
//
//        int realPercent = 2*100 / 4;
//
//
//
//        int  percent=30;
//
//        needSendAlarm &= !(realPercent < percent);
//
//        System.out.println(needSendAlarm);
//    }
//
//
//
//    public enum Type{
//        triangle,
//        rectangle,
//        circle,
//        sector
//    }
//
//
//
//
//    public double computeArea(){
//        return  0.0d;
//    }
//
//
//    public double computePerimeter(){
//        return  0.0d;
//    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<String> future = new FutureTask<String>(
                new Callable<String>() {
                    public String call() {
                        try {
                            Thread.sleep(1000);
                             for(int i =0;;i++){
                                 System.out.println(123);
                             }
                        } catch (Exception e) {
                            System.out.println("Thread InterruptedException");
                        }
                        return isTimout();
                    }
                });
        executor.execute(future);
        try {
            // 判断执行后

            String result = future.get(3000, TimeUnit.MILLISECONDS);
//            executor.
//            Thread.currentThread().stop();
            System.out.println("---->" + result);
            // 判断超时
            if ("Y".equals(result)) {
                // do something
                System.out.println("exec next method");
            }

        } catch (InterruptedException e) {
            future.cancel(true);
        } catch (ExecutionException e) {
            future.cancel(true);
        } catch (TimeoutException e) {
            future.cancel(true);
        } finally {
            executor.shutdown();
        }
    }

    private static String isTimout() {
        return "Y";
    }
}
