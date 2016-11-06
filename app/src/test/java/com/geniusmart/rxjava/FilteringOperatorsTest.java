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
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 2016/11/2.
 * Operators that selectively emit items from a source Observable.
 */
public class FilteringOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
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

    @Test
    public void distinct() {
        Observable.just(1, 2, 2, 1, 3)
                .distinct()
                .subscribe(System.out::println);
    }

    @Test
    public void distinctUtilChange() {
        Observable.just(1, 2, 2, 1, 3)
                .distinctUntilChanged()
                .subscribe(System.out::println);
    }

    @Test
    public void elementAt() {
        Observable.just(1, 2, 3, 4)
                .elementAt(2)
                .subscribe(System.out::println);
    }

    @Test
    public void filter() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .filter(integer -> integer > 10)
                .subscribe(System.out::println);
    }

    /**
     * find为RxPY操作符，这里使用filter+first来实现
     * http://rxmarbles.com/#find
     */
    @Test
    public void find() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .filter(integer -> integer > 10)
                .first()
                .subscribe(System.out::println);
    }

    @Test
    public void first() {
        Observable.just(1, 2, 3, 4)
                .first()
                .subscribe(System.out::println);
    }

    //TODO
    @Test
    public void IgnoreElements(){

    }


    @Test
    public void last() {
        Observable.just(1, 2, 3, 4)
                .last()
                .subscribe(System.out::println);
    }

    //TODO-此为RxJs的操作符
    @Test
    public void pausable() {
        Observable.just(1);
    }

    //TODO
    @Test
    public void pausableBuffered() {

    }

    @Test
    public void skip() {
        Observable.just(1, 2, 3, 4)
                .skip(2)
                .subscribe(System.out::println);
    }

    @Test
    public void skipLast() {
        Observable.just(1, 2, 3, 4)
                .skipLast(2)
                .subscribe(System.out::println);
    }

    @Test
    public void take() {
        Observable.just(1, 2, 3, 4)
                .take(2)
                .subscribe(System.out::println);
    }

    @Test
    public void takeLast() {
        Observable.just(1, 2, 3, 4)
                .takeLast(1)
                .subscribe(System.out::println);
    }

    //TODO 与结果不一样？？？
    @Test
    public void sample(){

        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                sleep(500);
                subscriber.onNext(2);
                sleep(500);
                subscriber.onNext(3);
                sleep(500);
                subscriber.onNext(4);
                sleep(500);
                subscriber.onNext(5);
                sleep(500);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println);

        Observable<String> observable2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                sleep(250);
                subscriber.onNext("A");
                sleep(300);
                subscriber.onNext("B");
                sleep(100);
                subscriber.onNext("C");
                sleep(1000);
                subscriber.onNext("D");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(System.out::println);


        observable1.sample(observable2)
                .subscribe(mList::add);

        advanceTimeAndPrint(10000);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void advanceTimeAndPrint(long delayTime) {
        mTestScheduler.advanceTimeBy(delayTime, TimeUnit.SECONDS);
        System.out.println(mList);
    }
}
