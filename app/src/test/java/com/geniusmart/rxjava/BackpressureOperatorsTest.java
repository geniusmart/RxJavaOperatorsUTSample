package com.geniusmart.rxjava;

import com.geniusmart.rxjava.utils.ControlledPullSubscriber;
import com.geniusmart.rxjava.utils.OperatorUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

/**
 * Created by geniusmart on 16/11/6.
 * <p>
 * strategies for coping with Observables that produce items more rapidly than their observers
 * consume them
 */
public class BackpressureOperatorsTest {

    //TODO 结合http://rxmarbles.com/#pausable

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    @Test
    public void onBackpressureBuffer() {

        ControlledPullSubscriber<Integer> puller =
                new ControlledPullSubscriber<>(System.out::println);

        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Integer integer) {
                OperatorUtils.logThread("Subscriber");
                System.out.println("Subscriber"+integer);
                OperatorUtils.sleep(200);
            }
        };

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                OperatorUtils.logThread("Observable");
                for(int i=1;i<1000000;i++){
                    System.out.println("Observable emit "+i);
                    subscriber.onNext(i);
                }

                //OperatorUtils.sleep(2000);

                for(int i=1;i<10000;i++){
                    subscriber.onNext(2);
                }

            }
        })
                //.onBackpressureDrop()
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(subscriber);

//        puller.requestMore(1);
//        puller.requestMore(1);
//        OperatorUtils.sleep(3000);
//        puller.requestMore(1);
//        OperatorUtils.sleep(3000);
//        puller.requestMore(1);
//        OperatorUtils.sleep(3000);
//        puller.requestMore(1);
//        OperatorUtils.sleep(3000);
//        puller.requestMore(10);
//        OperatorUtils.sleep(3000);
        OperatorUtils.sleep(1000000);
    }

    @Test
    public void onBackpressureBuffer1() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer(1000)
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    System.out.println(aLong);
                    OperatorUtils.sleep(100);
                });

        OperatorUtils.sleep(2000);
    }

    //http://blog.chengyunfeng.com/?p=981
    @Test
    public void onBackpressureDrop1() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(Schedulers.newThread())
                .subscribe(
                        i -> {
                            System.out.println(i);
                            try {
                                Thread.sleep(10);
                            } catch (Exception e) {
                            }
                        },
                        System.out::println);
        OperatorUtils.sleep(2000);
    }

    @Test
    public void onBackpressureDrop2() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(Schedulers.newThread())
                .subscribe(
                        i -> {
                            System.out.println(i);
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
                        },
                        System.out::println);
        OperatorUtils.sleep(30000);
    }

    @Test
    public void test1(){
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(
                        i -> {
                            System.out.println(i);
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) { }
                        },
                        System.out::println);
        OperatorUtils.sleep(2000);
    }

}
