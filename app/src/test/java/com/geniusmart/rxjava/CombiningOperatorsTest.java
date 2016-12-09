package com.geniusmart.rxjava;

import com.geniusmart.rxjava.utils.OperatorUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.joins.Plan0;
import rx.observables.JoinObservable;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 2016/11/1.
 * <p>
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

        JoinObservable.when(then).toObservable().subscribe(mList::add);

        mTestScheduler.advanceTimeBy(50, TimeUnit.SECONDS);
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
                OperatorUtils.logThread("observable1");
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
        }).subscribeOn(mTestScheduler).doOnNext(System.out::println);

        Observable<String> observable2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OperatorUtils.logThread("observable2");
                OperatorUtils.sleep(250);
                subscriber.onNext("A");
                OperatorUtils.sleep(300);
                subscriber.onNext("B");
                OperatorUtils.sleep(500);
                subscriber.onNext("C");
                OperatorUtils.sleep(100);
                subscriber.onNext("D");
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).doOnNext(System.out::println);

        Observable.combineLatest(observable1, observable2,
                (Func2<Integer, String, Object>) (integer, s) -> integer + s).subscribe(mList::add);

        //测试线程提前一定时间，让observable1能顺利开始发送数据
        mTestScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        System.out.println(mList);
        assertEquals(mList, Arrays.asList("1A", "2A", "2B", "2C", "2D", "3D", "4D", "5D"));
    }

    //TODO-未完成-join

    /**
     * combine items emitted by two Observables whenever an item from one Observable is emitted
     * during a time window defined according to an item emitted by the other Observable
     *
     * @see <a href="http://reactivex.io/documentation/operators/join.html">ReactiveX operators
     * documentation: Join</a>
     */
    @Test
    public void join() {

        Observable<Integer> o1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                OperatorUtils.logThread("observable1");
                subscriber.onNext(1);
                OperatorUtils.sleep(500);
                subscriber.onNext(2);
                OperatorUtils.sleep(100);
                subscriber.onNext(3);
                OperatorUtils.sleep(500);
                subscriber.onNext(4);
                OperatorUtils.sleep(500);
                subscriber.onCompleted();
            }
        }).subscribeOn(mTestScheduler).doOnNext(System.out::println);

        Observable<String> o2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OperatorUtils.logThread("observable2");
                OperatorUtils.sleep(250);
                subscriber.onNext("A");
                OperatorUtils.sleep(600);
                subscriber.onNext("B");
                OperatorUtils.sleep(200);
                subscriber.onNext("C");
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread()).doOnNext(System.out::println);

        o1.join(o2, new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer num) {
                return Observable.create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        subscriber.onNext(num);
                        OperatorUtils.sleep(300);
                        subscriber.onCompleted();
                    }
                }).subscribeOn(mTestScheduler);
            }
        }, new Func1<String, Observable<String>>() {
            @Override
            public Observable<String> call(String s) {
                return Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        subscriber.onNext(s);
                        OperatorUtils.sleep(300);
                        subscriber.onCompleted();
                    }
                }).subscribeOn(mTestScheduler);
            }
        }, new Func2<Integer, String, Object>() {
            @Override
            public Object call(Integer integer, String s) {
                return integer + s;
            }
        }).subscribe(mList::add);

        mTestScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        System.out.println(mList);
    }

    /**
     * combine multiple Observables into one by merging their emissions
     *
     * @see <a href = "http://rxmarbles.com/#merge">RxMarbles merge</a>
     * @see <a href="http://reactivex.io/documentation/operators/merge.html">ReactiveX operators
     * documentation: Merge</a>
     */
    @Test
    public void merge() {
        Observable<Long> observable1 = Observable.interval(5, TimeUnit.SECONDS, mTestScheduler)
                .take(5)
                .map(aLong -> (aLong + 1) * 20)
                .doOnNext(System.out::println);

        Observable<Long> observable2 = Observable.interval(18, TimeUnit.SECONDS, mTestScheduler)
                .take(2)
                .map(aLong -> 1L)
                .doOnNext(System.out::println);

        Observable.merge(observable1, observable2).subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1000, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(20L, 40L, 60L, 1L, 80L, 100L, 1L));
    }

    /**
     * emit a specified sequence of items before beginning to emit the items from the source
     * Observable
     *
     * @see <a href="http://rxmarbles.com/#startWith">Rxmarbles diagrams startWith</a>
     * @see <a href="http://reactivex.io/documentation/operators/startwith.html">ReactiveX operators
     * documentation: StartWith</a>
     */
    @Test
    public void startWith() {
        Observable.just(2, 3).startWith(1).subscribe(System.out::println);
    }

    /**
     * convert an Observable that emits Observables into a single Observable that emits the items
     * emitted by the most-recently-emitted of those Observables
     *
     * @see <a href="http://reactivex.io/documentation/operators/switch.html">ReactiveX operators
     * documentation: Switch</a>
     */
    @Test
    public void switchOnNext() {
        Observable<Integer> o1 = Observable.just(1, 2, 3);
        Observable<String> o2 = Observable.just("A", "B", "C");

        Observable.switchOnNext(Observable.just(o1, o2)).subscribe(mList::add);
        System.out.println(mList);
        assertEquals(mList, Arrays.asList(1, 2, 3, "A", "B", "C"));
    }

    /**
     * convert an Observable that emits Observables into a single Observable that emits the items
     * emitted by the most-recently-emitted of those Observables
     * <p>
     * 此例根据官方 marble diagrams 实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/switch.c.png">switch.png</a>
     */
    @Test
    public void switchOnNext2() {

        Observable<Long> o1 = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(3)
                .map(num -> num + 1)
                .doOnNext(System.out::println);

        Observable<String> o2 = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(3)
                .map(num -> String.valueOf((char) ('A' + num)))
                .doOnNext(System.out::println);

        Observable<? extends Observable<? extends Object>> observable =
                Observable.create(new Observable.OnSubscribe<Observable<? extends Object>>() {
                    @Override
                    public void call(Subscriber<? super Observable<? extends Object>> subscriber) {
                        subscriber.onNext(o1);
                        OperatorUtils.sleep(1500);
                        subscriber.onNext(o2);
                        OperatorUtils.sleep(3000);
                        subscriber.onCompleted();
                    }
                });

        Observable.switchOnNext(observable).subscribe(mList::add);

        System.out.println(mList);
        assertEquals(mList, Arrays.asList(1L, 2L, "A", "B", "C"));
    }

    /**
     * @see <a href="http://rxmarbles.com/#withLatestFrom">Rxmarbles diagrams withLatestFrom</a>
     * @see <a href="http://reactivex.io/documentation/operators/combinelatest.html">ReactiveX
     * operators documentation: CombineLatest</a>
     */
    @Test
    public void withLatestFrom() {
        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                OperatorUtils.sleep(500);
                subscriber.onNext(2);
                OperatorUtils.sleep(1500);
                subscriber.onNext(3);
                OperatorUtils.sleep(250);
                subscriber.onNext(4);
                OperatorUtils.sleep(500);
                subscriber.onNext(5);
            }
        }).subscribeOn(mTestScheduler).doOnNext(System.out::println);

        Observable<String> observable2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OperatorUtils.sleep(250);
                subscriber.onNext("A");
                OperatorUtils.sleep(300);
                subscriber.onNext("B");
                OperatorUtils.sleep(500);
                subscriber.onNext("C");
                OperatorUtils.sleep(100);
                subscriber.onNext("D");
            }
        }).subscribeOn(Schedulers.newThread()).doOnNext(System.out::println);

        observable1.withLatestFrom(observable2, (integer, s) -> integer + s).subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        assertEquals(mList, Arrays.asList("2A", "3D", "4D", "5D"));
    }

    /**
     * combine the emissions of multiple Observables together via a specified function and emit
     * single items for each combination based on the results of this function
     *
     * @see <a href="http://rxmarbles.com/#zip">Rxmarbles diagrams zip</a>
     * @see <a href="http://reactivex.io/documentation/operators/zip.html">ReactiveX operators
     * documentation: Zip</a>
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
