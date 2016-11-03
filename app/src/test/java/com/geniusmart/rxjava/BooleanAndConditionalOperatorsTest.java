package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 16/11/2.
 */
public class BooleanAndConditionalOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * RxJs的every操作符相当于RxJava的all
     * http://rxmarbles.com/#every
     */
    @Test
    public void every() {
        Observable.just(1, 2, 3, 4, 5)
                .doOnNext(System.out::println)
                .all(x -> x < 10)
                .subscribe(System.out::println);
    }

    //TODO--可以作为例子，一旦符合条件则停止发射
    /**
     * RxJs的some操作符相当于RxJava的exists
     * http://rxmarbles.com/#some
     */
    @Test
    public void some() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .doOnNext(System.out::println)
                .exists(integer -> integer > 10)
                .subscribe(System.out::println);
    }

    /**
     * includes相当于RxJava的contains
     * http://rxmarbles.com/#includes
     */
    @Test
    public void includes() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .contains(22)
                .subscribe(System.out::println);
    }

    //TODO--可以作为范例
    /**
     * SequenceEqual — determine whether two Observables emit the same sequence of items
     */
    @Test
    public void sequenceEqual() {

        Observable<Long> o1 = Observable.just(1L, 2L, 3L)
                .delay(1000, TimeUnit.MILLISECONDS, mTestScheduler);
        Observable<Long> o2 = Observable.interval(20, TimeUnit.MILLISECONDS, mTestScheduler)
                .skip(1)
                .take(3);

        Observable.sequenceEqual(o1, o2)
                .subscribe(mList::add);
        advanceTimeAndPrint(1000);
    }

    /**
     * Amb — given two or more source Observables, emit all of the items from only the first of these Observables to emit an item
     */
    @Test
    public void amb() {
        Observable<Integer> o1 = Observable.just(20, 40, 60).delay(500, TimeUnit.MILLISECONDS, mTestScheduler);
        Observable<Integer> o2 = Observable.just(1, 2, 3).delay(200, TimeUnit.MILLISECONDS, mTestScheduler);
        Observable<Integer> o3 = Observable.just(0, 0, 0).delay(1000, TimeUnit.MILLISECONDS, mTestScheduler);
        Observable.amb(o1, o2, o3)
                .subscribe(mList::add);

        advanceTimeAndPrint(1000);
        assertEquals(mList, Arrays.asList(1, 2, 3));
    }

    private void advanceTimeAndPrint(long delayTime) {
        mTestScheduler.advanceTimeBy(delayTime, TimeUnit.SECONDS);
        System.out.println(mList);
    }

}
