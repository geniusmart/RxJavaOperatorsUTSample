package com.geniusmart.rxjava;

import com.geniusmart.rxjava.utils.Utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func2;
import rx.joins.Plan0;
import rx.observables.JoinObservable;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

//TODO code style

/**
 * Created by geniusmart on 2016/11/1.
 * Operators that work with multiple source Observables to create a single Observable
 */
public class CombiningOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * combine sets of items emitted by two or more Observables by means of
     * Pattern and Plan intermediaries
     *
     * @see <a href="http://reactivex.io/documentation/operators/and-then-when.html">and/then/when</a>
     */
    @Test
    public void and_then_when() {

        Observable<Long> observable1 = Observable.interval(5, TimeUnit.SECONDS, mTestScheduler)
                .skip(1)
                .take(5)
                .doOnNext(System.out::println);
        Observable<Character> observable2 = Observable.interval(8, TimeUnit.SECONDS, mTestScheduler)
                .take(4)
                .map(aLong -> (char) ('A' + aLong))
                .doOnNext(System.out::println);


        Plan0<String> then = JoinObservable.from(observable1)
                .and(observable2)
                .then((aLong, aChar) -> aLong + String.valueOf(aChar));

        JoinObservable.when(then)
                .toObservable()
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(50,TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList("1A", "2B", "3C", "4D"));
    }

    /**
     * when an item is emitted by either of two Observables, combine the latest
     * item emitted by each Observable via a specified function and emit items
     * based on the results of this function
     *
     * @see <a href = "http://rxmarbles.com/#combineLatest">RxMarbles combineLatest diagrams</a>
     */
    @Test
    public void combineLatest() {

        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("observable1-->" + Thread.currentThread().getName());
                subscriber.onNext(1);
                Utils.sleep(500);
                subscriber.onNext(2);
                Utils.sleep(1500);
                subscriber.onNext(3);
                Utils.sleep(250);
                subscriber.onNext(4);
                Utils.sleep(500);
                subscriber.onNext(5);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println);

        Observable<String> observable2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                System.out.println("observable2-->" + Thread.currentThread().getName());
                Utils.sleep(250);
                subscriber.onNext("A");
                Utils.sleep(300);
                subscriber.onNext("B");
                Utils.sleep(500);
                subscriber.onNext("C");
                Utils.sleep(100);
                subscriber.onNext("D");
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(System.out::println);

        Observable.combineLatest(observable1, observable2,
                (Func2<Integer, String, Object>) (integer, s) -> integer + s)
                .subscribe(mList::add);

        //测试线程提前一定时间，让数据流能顺利完成订阅
        mTestScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        System.out.println(mList);
        assertEquals(mList, Arrays.asList("1A", "2A", "2B", "2C", "2D", "3D", "4D", "5D"));
    }

    //TODO
    /**
     *  combine items emitted by two Observables whenever an item from one Observable is emitted
     *  during a time window defined according to an item emitted by the other Observable
     */
    public void join() {

    }

    /**
     * combine multiple Observables into one by merging their emissions
     *
     * @see <a href = "http://rxmarbles.com/#merge">RxMarbles combineLatest merge</a>
     */
    @Test
    public void merge() {
        Observable<Long> observable1 = Observable.interval(5, TimeUnit.SECONDS, mTestScheduler)
                .take(5)
                .map(aLong -> (aLong + 1) * 20)
                .doOnNext(System.out::println);

        Observable<Integer> observable2 = Observable.interval(18, TimeUnit.SECONDS, mTestScheduler)
                .take(2)
                .map(aLong -> 1)
                .doOnNext(System.out::println);

        Observable.merge(observable1, observable2)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1000, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(20L, 40L, 60L, 1L, 80L, 100L, 1L));
    }

    /**
     * emit a specified sequence of items before beginning to emit the items from the source Observable
     *
     * @see <a href="http://rxmarbles.com/#startWith">Rxmarbles diagrams startWith</a>
     */
    @Test
    public void startWith() {
        Observable.just(2, 3)
                .startWith(1)
                .subscribe(System.out::println);
    }

    //TODO
    @Test
    public void switchOperator() {

    }

    /**
     * @see <a href="http://rxmarbles.com/#withLatestFrom">Rxmarbles diagrams startWith</a>
     */
    @Test
    public void withLatestFrom() {
        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(500);
                subscriber.onNext(2);
                Utils.sleep(1500);
                subscriber.onNext(3);
                Utils.sleep(250);
                subscriber.onNext(4);
                Utils.sleep(500);
                subscriber.onNext(5);
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println);

        Observable<String> observable2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Utils.sleep(250);
                subscriber.onNext("A");
                Utils.sleep(300);
                subscriber.onNext("B");
                Utils.sleep(500);
                subscriber.onNext("C");
                Utils.sleep(100);
                subscriber.onNext("D");
            }
        })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(System.out::println);

        observable1.withLatestFrom(observable2, (integer, s) -> integer + s)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        assertEquals(mList, Arrays.asList("2A", "3D", "4D", "5D"));

    }

    /**
     * combine the emissions of multiple Observables together via a specified function and emit
     * single items for each combination based on the results of this function
     *
     * @see <a href="http://rxmarbles.com/#zip">Rxmarbles diagrams zip</a>
     */
    @Test
    public void zip() {
        Observable<Long> observable1 = Observable.interval(5, TimeUnit.SECONDS, mTestScheduler)
                .skip(1)
                .take(5)
                .doOnNext(System.out::println);
        Observable<Character> observable2 = Observable.interval(8, TimeUnit.SECONDS, mTestScheduler)
                .take(4)
                .map(aLong -> (char) ('A' + aLong))
                .doOnNext(System.out::println);


        Observable.zip(observable1, observable2, (aLong, aChar) -> aLong + String.valueOf(aChar))
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(50, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList("1A", "2B", "3C", "4D"));
    }

}
