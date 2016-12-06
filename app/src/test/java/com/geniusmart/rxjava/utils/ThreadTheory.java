package com.geniusmart.rxjava.utils;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

/**
 * Created by geniusmart on 2016/11/10.
 * 测试原理介绍
 */
public class ThreadTheory {

    /**
     * 测试线程早于子线程执行完毕
     */
    @Test
    public void test_thread_early() {

        //测试线程启动
        System.out.println("测试线程-start");

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("子线程-start");
                OperatorUtils.sleep(3000);
                System.out.println("子线程-end");
            }
        }).start();

        //测试线程结束后，子线程还未执行完毕，因此子线程无法完整的输出测试结果
        System.out.println("测试线程-end");
    }

    /**
     * 测试线程晚于子线程执行完毕
     */
    @Test
    public void test_thread_late() {

        //测试线程启动，线程体需执行4s钟
        System.out.println("测试线程-start");

        //子线程能顺利执行完毕
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("子线程-start");
                OperatorUtils.sleep(3000);
                System.out.println("子线程-end");
            }
        }).start();

        OperatorUtils.sleep(4000);
        System.out.println("测试线程-end");
    }

    /**
     * 测试线程早于子线程执行完毕
     */
    @Test
    public void test_thread_early_observable() {
        System.out.println("测试线程-start,所在线程：" + Thread.currentThread().getName());

        //消息源在Schedulers.computation()线程中执行，3s后执行，此时测试线程已经执行完毕，无法正常输出结果
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(num -> {
                    System.out.println("Observable和Subscriber所在线程：" + Thread.currentThread().getName());
                    System.out.println("获取订阅数据：" + num);

                });
        System.out.println("测试线程-end");
    }

    /**
     * 让测试线程晚于子线程执行完毕
     */
    @Test
    public void test_thread_late_observable() {

        System.out.println("测试线程-start,所在线程：" + Thread.currentThread().getName());
        //消息源在Schedulers.computation()线程中执行，3s后执行
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(num -> {
                    System.out.println("Observable和Subscriber线程：" + Thread.currentThread().getName());
                    System.out.println("获取订阅数据：" + num);
                });

        // 测试线程在4s后结束，能保证子线程完整执行
        OperatorUtils.sleep(4000);
        System.out.println("测试线程-end");
    }

    /**
     * 使用TestScheduler将线程设置为测试线程，并将时间提前
     */
    @Test
    public void test_thread_with_TestScheduler() {

        TestScheduler testScheduler = Schedulers.test();
        System.out.println("测试线程：" + Thread.currentThread().getName());

        //指定调度器
        Observable.timer(3, TimeUnit.SECONDS, testScheduler)
                .subscribe(num -> {
                    System.out.println("Observable和Subscriber线程：" + Thread.currentThread().getName());
                    System.out.println("获取订阅数据：" + num);
                });

        //将时间提前了3s
        testScheduler.advanceTimeBy(3, TimeUnit.SECONDS);
    }

    /**
     * 聚合操作符的线程处理方式一：两个Observable分别在两条子线程中执行，且测试线程的生命周期比2条子线程更长
     */
    @Test
    public void combiningOperator_thread_way1() {

        //observable1在子线程1中执行
        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("observable1-->" + Thread.currentThread().getName());
                subscriber.onNext(1);
                OperatorUtils.sleep(500);
                subscriber.onNext(2);
                OperatorUtils.sleep(1500);
                subscriber.onNext(3);
                OperatorUtils.sleep(250);
                subscriber.onNext(4);
                OperatorUtils.sleep(500);
                subscriber.onNext(5);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread());

        //observable2在子线程2中执行
        Observable<Integer> observable2 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                OperatorUtils.sleep(200);
                System.out.println("observable2-->" + Thread.currentThread().getName());
                subscriber.onNext(1111);
                subscriber.onNext(2222);
                subscriber.onNext(3333);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread());

        Observable.merge(observable1, observable2).subscribe(System.out::println);

        //测试线程休眠一定时间，保证两个消息源所在线程能正常执行完毕
        OperatorUtils.sleep(5000);

    }

    /**
     * 聚合操作符的线程处理方式二：一个Observable在TestScheduler线程中执行，另外一个Observable在子线程中
     * 执行，并将时钟提前，保证Observable1能顺利执行完毕
     */
    @Test
    public void combiningOperator_thread_way2() {

        TestScheduler testScheduler = new TestScheduler();

        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("observable1-->" + Thread.currentThread().getName());
                subscriber.onNext(1);
                OperatorUtils.sleep(500);
                subscriber.onNext(2);
                OperatorUtils.sleep(1500);
                subscriber.onNext(3);
                OperatorUtils.sleep(250);
                subscriber.onNext(4);
                OperatorUtils.sleep(500);
                subscriber.onNext(5);
                subscriber.onCompleted();
            }
        }).subscribeOn(testScheduler);

        Observable<Integer> observable2 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                OperatorUtils.sleep(200);
                System.out.println("observable2-->" + Thread.currentThread().getName());
                subscriber.onNext(1111);
                subscriber.onNext(2222);
                subscriber.onNext(3333);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread());

        Observable.merge(observable, observable2).subscribe(System.out::println);

        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);

    }

}
