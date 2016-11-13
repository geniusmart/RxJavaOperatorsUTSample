package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.BlockingObservable;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 16/11/6.
 * <p>
 * convert an Observable into another object or data structure
 *
 * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX documentation: To</a>
 */
public class ConvertObservablesTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * Returns an {@link Iterator} that iterates over all items emitted by BlockingObservable.
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/B.toIterable.png">
     * getIterator</a>
     */
    @Test
    public void getIterator() {

        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS)
                .take(3);

        Iterator<Long> iterator = BlockingObservable.from(observable)
                .getIterator();

        while (iterator.hasNext()) {
            mList.add(iterator.next());
        }
        assertEquals(mList, Arrays.asList(0L, 1L, 2L));

    }

    /**
     * Converts an Observable into a BlockingObservable(an Observable with blocking operators).
     */
    @Test
    public void toBlocking() {

        Observable.interval(1, TimeUnit.SECONDS)
                .take(3)
                .toBlocking()
                .forEach(mList::add);

        assertEquals(mList, Arrays.asList(0L, 1L, 2L));

    }

    /**
     * Returns a {@link Future} representing the single value emitted by this BlockingObservable.
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/B.toFuture.png">
     * toFuture</a>
     */
    @Test
    public void toFuture() throws ExecutionException, InterruptedException {

        //只能发射一个数据
        Future<Long> future = Observable.timer(1, TimeUnit.SECONDS)
                .toBlocking()
                .toFuture();

        long aLong = future.get();
        assertEquals(aLong,0L);
    }

    /**
     * Converts this BlockingObservable into an {@link Iterable}.
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/B.toIterable.png">
     * toIterable</a>
     */
    @Test
    public void toIterable() {

        Iterable<Integer> iterable = Observable.just(1, 2, 3)
                .toBlocking()
                .toIterable();

        Iterator<Integer> iterator = iterable.iterator();

        while (iterator.hasNext()) {
            mList.add(iterator.next());
        }
        assertEquals(mList, Arrays.asList(1, 2, 3));

    }

    /**
     * Returns an Observable that emits a single item, a list composed of all the
     * items emitted by the source Observable.
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/toList.png">toList</a>
     */
    @Test
    public void toList() {

        Observable.interval(1, TimeUnit.SECONDS, mTestScheduler)
                .take(6)
                .toList()
                .doOnNext(num -> {
                    System.out.println("只触发一次,value=" + num);
                })
                .subscribe(mList::addAll);

        mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);

        assertEquals(mList, Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L));
    }

    /**
     * Returns an Observable that emits a single HashMap containing all items emitted by the source Observable,
     * mapped by the keys returned by a specified function
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/toMap.png">toMap</a>
     */
    @Test
    public void toMap() {

        Map<String, String> map = new HashMap<>();

        Observable.just("one", "two", "three")
                .toMap(s -> "key" + s)
                .subscribe(map::putAll);

        System.out.println(map);
    }

    /**
     * Returns an Observable that emits a single HashMap containing all items emitted by the source Observable,
     * mapped by the keys returned by a specified function
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/toMultiMap.png">toMap</a>
     */
    @Test
    public void toMultiMap() {

        Observable.just(Arrays.asList(1,2),Arrays.asList("A","B"))
                .toMultimap(new Func1<List<? extends Serializable>, Object>() {
                    @Override
                    public Object call(List<? extends Serializable> list) {
                        return list.get(0);
                    }
                })
                .subscribe(System.out::println);
    }

    /**
     * @see <a href="http://reactivex.io/documentation/operators/images/toSortedList.png">toSortedList</a>
     */
    @Test
    public void toSortedList() {

        Observable.just(2, 5, 1, 6, 3, 4)
                .toSortedList()
                .subscribe(mList::addAll);
        assertEquals(mList, Arrays.asList(1, 2, 3, 4, 5, 6));

        //以下代码进行倒序
        mList.clear();
        Observable.just(2, 5, 1, 6, 3, 4)
                .toSortedList((integer1, integer2) -> integer2 - integer1)
                .subscribe(mList::addAll);
        assertEquals(mList, Arrays.asList(6, 5, 4, 3, 2, 1));
    }

    /**
     * Converts the source Observable<T> into an Observable<Observable<T>> that emits the
     * source Observable as its single emission.
     *
     * @see <a href="http://reactivex.io/documentation/operators/to.html">ReactiveX operators documentation: To</a>
     */
    @Test
    public void nest() {

        Observable<Integer> observable = Observable.just(1, 2, 3);

        //将observable本身作为数据流发射出去
        observable.nest()
                .subscribe(mList::add);

        assertEquals(mList, Collections.singletonList(observable));
    }
}
