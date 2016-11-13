package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observables.MathObservable;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 16/11/2.
 * <p>
 * Operators that operate on the entire sequence of items emitted by an Observable
 */
public class MathematicalAndAggregateOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * calculates the average of numbers emitted by an Observable and emits this average
     *
     * @see <a href="http://rxmarbles.com/#average">RxMarbles diagrams average</a>
     */
    @Test
    public void average() {
        Observable<Integer> observable = Observable.just(1, 2, 2, 2, 5);
        MathObservable.averageInteger(observable)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(2));

        mList.clear();
        MathObservable.averageDouble(Observable.just(1.0, 2.0, 2.0, 2.0, 5.0))
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(2.4));
    }

    /**
     * emit the emissions from two or more Observables without interleaving them
     *
     * @see <a href="http://rxmarbles.com/#concat">RxMarbles diagrams concat</a>
     */
    @Test
    public void concat() {
        Observable<Integer> observable1 = Observable.interval(10, TimeUnit.SECONDS, mTestScheduler)
                .map(aLong -> 1)
                .take(3);

        Observable<Integer> observable2 = Observable.interval(1, TimeUnit.SECONDS, mTestScheduler)
                .map(aLong -> 2)
                .take(2);

        Observable.concat(observable1, observable2)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);

        assertEquals(mList, Arrays.asList(1,1,1,2,2));
    }

    /**
     * count the number of items emitted by the source Observable and emit only this value
     *
     * @see <a href="http://rxmarbles.com/#count">RxMarbles diagrams count</a>
     */
    @Test
    public void count() {
        Observable.just(1, 2, 3, 4).count()
                .subscribe(mList::add);

        assertEquals(mList, Collections.singletonList(4));
    }

    /**
     * determine, and emit, the maximum-valued item emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#max">RxMarbles diagrams max</a>
     */
    @Test
    public void max() {
        MathObservable.max(Observable.just(2, 30, 22, 5, 60, 1))
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(60));
    }

    /**
     * determine, and emit, the minimum-valued item emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#min">RxMarbles diagrams min</a>
     */
    @Test
    public void min() {
        MathObservable.min(Observable.just(2, 30, 22, 5, 60, 1))
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(1));
    }

    /**
     * apply a function to each item emitted by an Observable, sequentially, and emit the final value
     *
     * @see <a href="http://rxmarbles.com/#reduce">RxMarbles diagrams reduce</a>
     */
    @Test
    public void reduce() {
        Observable.just(1, 2, 3, 4, 5)
                .reduce((num1, num2) -> num1 + num2)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(15));
    }

    /**
     * calculate the sum of numbers emitted by an Observable and emit this sum
     *
     * @see <a href="http://rxmarbles.com/#sum">RxMarbles diagrams sum</a>
     */
    @Test
    public void sum() {
        MathObservable.sumInteger(Observable.just(1, 2, 3, 4, 5))
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(15));
    }

}
