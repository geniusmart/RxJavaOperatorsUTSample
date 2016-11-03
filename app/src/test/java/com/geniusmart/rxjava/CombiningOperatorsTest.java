package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

//TODO code style
/**
 * Created by geniusmart on 2016/11/1.
 */
public class CombiningOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    @Test
    public void combineLatest(){

        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("observable1-->"+Thread.currentThread().getName());
                subscriber.onNext(1);
                sleep(500);
                subscriber.onNext(2);
                sleep(1500);
                subscriber.onNext(3);
                sleep(250);
                subscriber.onNext(4);
                sleep(500);
                subscriber.onNext(5);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println);

        Observable<String> observable2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                System.out.println("observable2-->"+Thread.currentThread().getName());
                sleep(250);
                subscriber.onNext("A");
                sleep(300);
                subscriber.onNext("B");
                sleep(500);
                subscriber.onNext("C");
                sleep(100);
                subscriber.onNext("D");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(System.out::println);

        Observable.combineLatest(observable1, observable2,
                (Func2<Integer, String, Object>) (integer, s) -> integer + s)
                .subscribe(mList::add);

        advanceTimeAndPrint(10000);
    }

    @Test
    public void concat(){
        Observable<Integer> observable1 = Observable.interval(10, TimeUnit.SECONDS, mTestScheduler)
                .map(aLong -> 1)
                .take(3);

        Observable<Integer> observable2 = Observable.interval(1, TimeUnit.SECONDS, mTestScheduler)
                .map(aLong -> 2)
                .take(2);

        Observable.concat(observable1,observable2)
                .subscribe(mList::add);
        advanceTimeAndPrint(100);
    }

    @Test
    public void merge() {
        Observable<Long> observable1 = Observable.interval(5, TimeUnit.SECONDS, mTestScheduler)
                .take(5)
                .map(aLong -> (aLong + 1) * 20);

        Observable<Integer> observable2 = Observable.interval(18, TimeUnit.SECONDS, mTestScheduler)
                .take(2)
                .map(aLong -> 1);

        Observable.merge(observable1,observable2)
                .subscribe(mList::add);

        advanceTimeAndPrint(100);
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

    @Test
    public void startWith(){
        Observable.just(2,3)
                .startWith(1)
                .subscribe(System.out::println);
    }

    @Test
    public void withLatestFrom(){
        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                sleep(500);
                subscriber.onNext(2);
                sleep(1500);
                subscriber.onNext(3);
                sleep(250);
                subscriber.onNext(4);
                sleep(500);
                subscriber.onNext(5);
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
                sleep(500);
                subscriber.onNext("C");
                sleep(100);
                subscriber.onNext("D");
            }
        })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(System.out::println);

        observable1.withLatestFrom(observable2, (integer, s) -> integer + s)
                .subscribe(mList::add);
        advanceTimeAndPrint(10000);

    }

    @Test
    public void zip(){
        Observable<Long> observable1 = Observable.interval(5, TimeUnit.SECONDS, mTestScheduler)
                .skip(1)
                .take(5);
        Observable<Character> observable2 = Observable.interval(8, TimeUnit.SECONDS, mTestScheduler)
                .take(4)
                .map(aLong -> (char) ('A' + aLong));

        Observable.zip(observable1, observable2, (aLong, aChar) -> aLong + String.valueOf(aChar))
                .subscribe(mList::add);

        advanceTimeAndPrint(1000);
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
