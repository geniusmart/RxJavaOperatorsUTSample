package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.TestScheduler;

/**
 * Created by geniusmart on 2016/11/2.
 */
public class TransformingOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    @Test
    public void delay(){
        Observable.just(1,2,1)
                .delay(3000, TimeUnit.SECONDS, mTestScheduler)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(2000,TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);

        mTestScheduler.advanceTimeBy(3000,TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);

    }

    //TODO：RxJs的操作符
    @Test
    public void delayWithSelector(){
        Observable.just(1,2,1)
                .delay(new Func1<Integer, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Integer integer) {
                        return Observable.timer(integer*20,TimeUnit.SECONDS);
                    }
                }).subscribe(mList::add);
        mTestScheduler.advanceTimeBy(3000,TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
    }

    //TODO：RxJs的操作符
    @Test
    public void findIndex(){

    }

    @Test
    public void map(){
        Observable.just(1,2,3)
                .map(integer -> integer * 10)
                .subscribe(System.out::println);
    }

    @Test
    public void scan(){
        Observable.just(1,2,3,4,5)
                .scan((num1, num2) -> num1 + num2)
                .subscribe(System.out::println);

    }

    //TODO 如何来理解？
    @Test
    public void debounce(){

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                sleep(500);

                subscriber.onNext(2);
                sleep(10);
                subscriber.onNext(3);
                sleep(20);
                subscriber.onNext(4);
                sleep(30);
                subscriber.onNext(5);

                sleep(500);
                subscriber.onNext(6);
                sleep(500);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println)
                .debounce(400,TimeUnit.MILLISECONDS)
                .subscribe(mList::add);

        advanceTimeAndPrint(10000);

    }

    @Test
    public void debounce2(){
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                    //产生结果的间隔时间分别为100、200、300...900毫秒
                    for (int i = 1; i < 10; i++) {
                        subscriber.onNext(i);
                        sleep(i*100);
                    }
                    subscriber.onCompleted();
            }
        }).subscribeOn(mTestScheduler)
                .debounce(400, TimeUnit.MILLISECONDS)  //超时时间为400毫秒
                .subscribe(mList::add);

        advanceTimeAndPrint(10000);

    }

    //TODO-如何理解
    @Test
    public void debounceWithSelector(){
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                //产生结果的间隔时间分别为100、200、300...900毫秒
                for (int i = 1; i < 10; i++) {
                    subscriber.onNext(i);
                    sleep(i*100);
                }
                subscriber.onCompleted();
            }
        }).debounce(new Func1<Integer, Observable<Long>>() {
            @Override
            public Observable<Long> call(Integer integer) {
                return Observable.timer(integer*10,TimeUnit.SECONDS);
            }
        });
    }

    private void advanceTimeAndPrint(long delayTime) {
        mTestScheduler.advanceTimeBy(delayTime, TimeUnit.SECONDS);
        System.out.println(mList);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
