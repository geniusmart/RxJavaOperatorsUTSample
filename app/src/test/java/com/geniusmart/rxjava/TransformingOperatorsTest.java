package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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
    public void delay() {
        Observable.just(1, 2, 1)
                .delay(3000, TimeUnit.SECONDS, mTestScheduler)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(2000, TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
        assertTrue(mList.isEmpty());

        mTestScheduler.advanceTimeBy(3000, TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
        assertEquals(mList, Arrays.asList(1, 2, 1));

    }

    //TODO：RxJs的操作符
    @Test
    public void delayWithSelector() {

        Observable.just(1, 2, 1)
                .delay(new Func1<Integer, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Integer integer) {
                        return Observable.timer(integer * 20, TimeUnit.SECONDS);
                    }
                }).subscribe(mList::add);
        mTestScheduler.advanceTimeBy(3000, TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
    }

    //TODO：再确定
    /**
     * RxJs的操作符，RxJava无对应的操作符
     * http://rxmarbles.com/#findIndex
     */
    @Test
    public void findIndex() {

    }

    @Test
    public void map() {
        Observable.just(1, 2, 3)
                .map(integer -> integer * 10)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(10, 20, 30));
    }

    @Test
    public void scan() {

        Observable.just(1, 2, 3, 4, 5)
                .scan((num1, num2) -> num1 + num2)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 3, 6, 10, 15));

    }

    /**
     * Observable每产生一个结果后，如果在规定的间隔时间内没有别的结果产生，则把这个结果提交给订阅者处理，否则忽略该结果。
     */
    @Test
    public void debounce() {

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                sleep(500);

                subscriber.onNext(2);
                sleep(100);
                subscriber.onNext(3);
                sleep(200);
                subscriber.onNext(4);
                sleep(300);
                subscriber.onNext(5);

                sleep(500);
                subscriber.onNext(6);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println)
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(mList::add);

        advanceTimeAndPrint(10000);
        assertEquals(mList,Arrays.asList(1,5,6));
    }

    //TODO-如何理解
    @Test
    public void debounceWithSelector() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                //产生结果的间隔时间分别为100、200、300...900毫秒
                for (int i = 1; i < 10; i++) {
                    subscriber.onNext(i);
                    sleep(i * 100);
                }
                subscriber.onCompleted();
            }
        }).debounce(new Func1<Integer, Observable<Long>>() {
            @Override
            public Observable<Long> call(Integer integer) {
                return Observable.timer(integer * 10, TimeUnit.SECONDS);
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
