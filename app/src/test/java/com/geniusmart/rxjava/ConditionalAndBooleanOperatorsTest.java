package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 16/11/2.
 * Operators that originate new Observables.
 */
public class ConditionalAndBooleanOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * determine whether all items emitted by an Observable meet some criteria
     *
     * @see <a href="http://rxmarbles.com/#every">RxMarbles diagrams all</a>
     */
    @Test
    public void all() {
        Observable.just(1, 2, 3, 4, 5)
                .doOnNext(System.out::println)
                .all(x -> x < 10)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(true));
    }

    /**
     * @see <a href="http://rxmarbles.com/#some">RxMarbles diagrams exists</a>
     */
    @Test
    public void exists() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .doOnNext(System.out::println)
                .exists(integer -> integer > 10)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(true));
    }

    /**
     * determine whether an Observable emits a particular item or not
     *
     * @see <a href="http://rxmarbles.com/#includes">RxMarbles diagrams contains</a>
     */
    @Test
    public void contains() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .contains(22)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(true));
    }

    /**
     * determine whether two Observables emit the same sequence of items
     *
     * @see <a href="http://rxmarbles.com/#sequenceEqual">RxMarbles diagrams exists</a>
     */
    @Test
    public void sequenceEqual() {

        Observable<Long> o1 = Observable.just(1L, 2L, 3L, 4L, 5L)
                .delay(1000, TimeUnit.SECONDS, mTestScheduler);
        Observable<Long> o2 = Observable.interval(20, TimeUnit.SECONDS, mTestScheduler)
                .skip(1)
                .take(5);

        Observable.sequenceEqual(o1, o2)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1200, TimeUnit.SECONDS);

        assertEquals(mList, Collections.singletonList(true));

    }

    /**
     * given two or more source Observables, emit all of the items from only the first of these
     * Observables to emit an item
     *
     * @see <a href="http://rxmarbles.com/#amb">RxMarbles diagrams amb</a>
     */
    @Test
    public void amb() {
        Observable<Integer> o1 = Observable.just(20, 40, 60)
                .delay(500, TimeUnit.SECONDS, mTestScheduler);

        Observable<Integer> o2 = Observable.just(1, 2, 3)
                .delay(200, TimeUnit.SECONDS, mTestScheduler);

        Observable<Integer> o3 = Observable.just(0, 0, 0)
                .delay(1000, TimeUnit.SECONDS, mTestScheduler);

        Observable.amb(o1, o2, o3)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1000, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(1, 2, 3));
    }

    /**
     * emit items from the source Observable, or a default item if the source Observable
     * emits nothing
     *
     * @see <a href="http://reactivex.io/documentation/operators/defaultifempty.html">
     * defaultIfEmpty</a>
     */
    @Test
    public void defaultIfEmpty() {
        Observable.empty()
                .defaultIfEmpty("geniusmart")
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList("geniusmart"));
    }

    //TODO 可以作为范例

    /**
     * discard items emitted by an Observable until a second Observable emits an item
     *
     * @see <a href="http://rxmarbles.com/#skipUntil">RxMarbles diagrams skipUntil</a>
     */
    @Test
    public void skipUntil() {

        Observable<Long> o1 = Observable.interval(100, TimeUnit.SECONDS, mTestScheduler)
                .map(num -> num + 1)
                .take(9);

        Observable<Integer> o2 = Observable.just(0, 0)
                .delay(550, TimeUnit.SECONDS, mTestScheduler);

        o1.skipUntil(o2)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(2000, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(6L, 7L, 8L, 9L));
    }

    /**
     * discard items emitted by an Observable until a specified condition becomes false
     *
     * @see <a href="http://reactivex.io/documentation/operators/skipwhile.html">
     * skipWhile</a>
     */
    @Test
    public void skipWhile() {
        Observable.interval(100, TimeUnit.SECONDS, mTestScheduler)
                .map(num -> num + 1)
                .take(7)
                .skipWhile(aLong -> aLong != 4)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1000, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(4L, 5L, 6L, 7L));
    }

    /**
     * discard items emitted by an Observable after a second Observable emits an item or
     * terminates
     */
    @Test
    public void takeUntil() {
        Observable.just(1, 2, 3, 4)
                .takeUntil(integer -> integer > 2)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 3));

        mList.clear();
        Observable.just(1, 2, 3, 4)
                .takeUntil(integer -> integer < 10)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(1));
    }

    /**
     * TakeUntil — discard items emitted by an Observable after a second Observable emits an
     * item or terminates
     *
     * @see <a href="http://rxmarbles.com/#takeUntil">RxMarbles diagrams takeUntil</a>
     */
    @Test
    public void takeUntilWithObservable() {

        Observable<Integer> observable = Observable.just(0, 0)
                .delay(550, TimeUnit.MILLISECONDS, mTestScheduler);

        Observable.interval(0, 100, TimeUnit.MILLISECONDS, mTestScheduler)
                .skip(1)
                .takeUntil(observable)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(10, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(1L, 2L, 3L, 4L, 5L));

    }

    /**
     * discard items emitted by an Observable after a specified condition becomes false
     *
     * @see <a href="http://reactivex.io/documentation/operators/takewhile.html">
     * takeWhile</a>
     */
    @Test
    public void takeWhile() {
        Observable.interval(100, TimeUnit.SECONDS, mTestScheduler)
                .map(num -> num + 1)
                .take(7)
                .takeWhile(aLong -> aLong != 4)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1000, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(1L, 2L, 3L));
    }
}
