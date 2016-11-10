package com.geniusmart.rxjava.utils;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

/**
 * Created by geniusmart on 2016/11/10.
 */
public class Basic {

    //TODO 聚合操作符的线程原理
    /**
     * 测试线程早于子线程执行完毕
     */
    @Test
    public void test_thread_early() {

        //测试线程启动
        System.out.println("Test thread begin!");

        //测试线程结束后，子线程还未执行完毕，因此无法完整的输出测试结果
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("other thread begin!");
                Utils.sleep(3000);
                System.out.println("other thread end!");
            }
        }).start();

        System.out.println("Test thread end!");
    }

    /**
     * 测试线程晚于子线程执行完毕
     */
    @Test
    public void test_thread_late() {

        //测试线程启动，线程体需执行4s钟
        System.out.println("Test thread begin!");

        //子线程能顺利执行完毕
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Other thread begin!");
                Utils.sleep(3000);
                System.out.println("Other thread end!");
            }
        }).start();

        Utils.sleep(4000);
        System.out.println("Test thread end!");
    }

    /**
     * 测试线程早于子线程执行完毕
     */
    @Test
    public void test_thread_early_observable() {
        System.out.println(Thread.currentThread().getName());
        //消息源在Schedulers.computation()线程中执行，3s后执行，此时测试线程已经执行完毕，无法正常输出结果
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(num -> {
                    System.out.println("Observable和Subscriber线程：" + Thread.currentThread().getName());
                    System.out.println("获取订阅数据：" + num);

                });
    }

    /**
     * 让测试线程晚于子线程执行完毕
     */
    @Test
    public void test_thread_late_observable() {

        System.out.println("测试线程：" + Thread.currentThread().getName());
        //消息源在Schedulers.computation()线程中执行，3s后执行
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(num -> {
                    System.out.println("Observable和Subscriber线程：" + Thread.currentThread().getName());
                    System.out.println("获取订阅数据：" + num);
                });

        // 测试线程在4s后结束，能保证子线程完整执行
        Utils.sleep(4000);
    }

    /**
     * 使用TestScheduler将线程设置为测试线程，并将时间提前
     */
    @Test
    public void test_thread_with_TestSchedule() {

        TestScheduler testScheduler = Schedulers.test();
        System.out.println("测试线程：" + Thread.currentThread().getName());
        //消息源在Schedulers.computation()线程中执行，3s后执行，此时测试线程已经执行完毕，无法正常输出结果
        Observable.timer(3, TimeUnit.SECONDS, testScheduler)
                .subscribe(num -> {
                    System.out.println("Observable和Subscriber线程：" + Thread.currentThread().getName());
                    System.out.println("获取订阅数据：" + num);
                });

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);
    }

}
