package com.geniusmart.rxjava;

import com.geniusmart.rxjava.utils.Utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 2016/11/2.
 * <p>
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
     * only emit an item from an Observable if a particular timespan has passed without it
     * emitting another item
     * <p>
     * Observable每产生一个结果后，如果在规定的间隔时间内没有别的结果产生，则把这个结果提交给订阅者处理，否则忽略该结果。
     *
     * @see <a href="http://rxmarbles.com/#debounce">RxMarbles diagrams debounce</a>
     * @see <a href="http://reactivex.io/documentation/operators/debounce.html">ReactiveX operators
     * documentation: Debounce</a>
     */
    @Test
    public void debounce() {

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(500);

                subscriber.onNext(2);
                Utils.sleep(100);
                subscriber.onNext(3);
                Utils.sleep(200);
                subscriber.onNext(4);
                Utils.sleep(300);
                subscriber.onNext(5);

                Utils.sleep(500);
                subscriber.onNext(6);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println)
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(10, TimeUnit.SECONDS);
        System.out.println(mList);
        assertEquals(mList, Arrays.asList(1, 5, 6));
    }

    //TODO debounceWithSelector可作为范例

    /**
     * If the source Observable emits another item before this newly-generated Observable
     * terminates, debounce will suppress the item.
     * <p/>
     * 根据官方宝蓝图实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/debounce.f.png">debounce.png</a>
     */
    @Test
    public void debounceWithSelector() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(2000);
                //2所对应的timer Observable还未结束，3已经开始发送，因此2将被废弃
                subscriber.onNext(2);
                subscriber.onNext(3);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .debounce(integer -> Observable.timer(1, TimeUnit.SECONDS))
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        System.out.println(mList);
        assertEquals(mList, Arrays.asList(1, 3));
    }

    /**
     * suppress duplicate items emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#distinct">RxMarbles diagrams distinct</a>
     */
    @Test
    public void distinct() {
        Observable.just(1, 2, 2, 1, 3)
                .distinct()
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 3));
    }

    /**
     * @see <a href="http://rxmarbles.com/#distinctUntilChanged">RxMarbles diagrams
     * distinctUntilChanged</a>
     */
    @Test
    public void distinctUtilChange() {
        Observable.just(1, 2, 2, 1, 3)
                .distinctUntilChanged()
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 1, 3));
    }

    /**
     * emit only item n emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#elementAt">RxMarbles diagrams elementAt</a>
     */
    @Test
    public void elementAt() {
        Observable.just(1, 2, 3, 4)
                .elementAt(2)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(3));
    }

    /**
     * emit only those items from an Observable that pass a predicate test
     *
     * @see <a href="http://rxmarbles.com/#filter">RxMarbles diagrams filter</a>
     */
    @Test
    public void filter() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .filter(integer -> integer > 10)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(30, 22, 60));
    }

    /**
     * find为RxPY操作符，这里使用filter+first来实现
     *
     * @see <a href="http://rxmarbles.com/#find">RxMarbles diagrams find</a>
     */
    @Test
    public void find() {
        Observable.just(2, 30, 22, 5, 60, 1)
                .filter(integer -> integer > 10)
                .first()
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(30));
    }

    /**
     * emit only the first item, or the first item that meets a condition, from an Observable
     *
     * @see <a href="http://rxmarbles.com/#first">RxMarbles diagrams first</a>
     */
    @Test
    public void first() {
        Observable.just(1, 2, 3, 4)
                .first()
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(1));
    }

    /**
     * do not emit any items from an Observable but mirror its termination notification
     *
     * @see <a href="http://reactivex.io/documentation/operators/ignoreelements.html">ReactiveX
     * operators documentation: IgnoreElements</a>
     */
    @Test
    public void IgnoreElements() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .ignoreElements()
                .doOnCompleted(() -> mList.add("Completed"))
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList("Completed"));
    }


    /**
     * emit only the last item emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#last">RxMarbles diagrams last</a>
     */
    @Test
    public void last() {
        Observable.just(1, 2, 3, 4)
                .last()
                .subscribe(mList::add);

        assertEquals(mList, Collections.singletonList(4));
    }

    /**
     * suppress the first n items emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#skip">RxMarbles diagrams skip</a>
     */
    @Test
    public void skip() {
        Observable.just(1, 2, 3, 4)
                .skip(2)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(3, 4));
    }

    /**
     * suppress the last n items emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#skipLast">RxMarbles diagrams skipLast</a>
     */
    @Test
    public void skipLast() {
        Observable.just(1, 2, 3, 4)
                .skipLast(2)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2));
    }

    /**
     * emit only the first n items emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#take">RxMarbles diagrams take</a>
     */
    @Test
    public void take() {
        Observable.just(1, 2, 3, 4)
                .take(2)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2));
    }

    /**
     * emit only the last n items emitted by an Observable
     *
     * @see <a href="http://rxmarbles.com/#takeLast">RxMarbles diagrams takeLast</a>
     */
    @Test
    public void takeLast() {
        Observable.just(1, 2, 3, 4)
                .takeLast(1)
                .subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(4));
    }

    /**
     * emit the most recent item emitted by an Observable within periodic time intervals
     *
     * @see <a href="http://rxmarbles.com/#sample">RxMarbles diagrams sample</a>
     * @see <a href="http://reactivex.io/documentation/operators/sample.html">ReactiveX operators
     * documentation: Sample</a>
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Backpressure">RxJava wiki:
     * Backpressure</a>
     */
    @Test
    public void sample() {

        Observable<Integer> observable1 = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(500);
                subscriber.onNext(2);
                Utils.sleep(500);
                subscriber.onNext(3);
                Utils.sleep(500);
                subscriber.onNext(4);
                Utils.sleep(500);
                subscriber.onNext(5);
                Utils.sleep(500);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnNext(System.out::println)
                .doOnCompleted(() -> System.out.println("observable1-Completed"));

        Observable<String> observable2 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Utils.sleep(250);
                subscriber.onNext("A");
                Utils.sleep(300);
                subscriber.onNext("B");
                Utils.sleep(100);
                subscriber.onNext("C");
                Utils.sleep(1000);
                subscriber.onNext("D");
                Utils.sleep(500);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .doOnNext(System.out::println)
                .doOnCompleted(() -> System.out.println("observable2-Completed"));


        observable1.sample(observable2)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        System.out.println(mList);

        assertEquals(mList, Arrays.asList(1, 2, 4, 5));
    }

    /**
     * Returns an Observable that emits only the first item emitted by the source Observable during
     * sequential time windows of a specified duration.
     * <p/>
     * 根据官方宝蓝图实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/throttleFirst.png">throttleFirst.png</a>
     */
    @Test
    public void throttleFirst() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(500);
                subscriber.onNext(2);
                subscriber.onNext(3);
                Utils.sleep(500);
                subscriber.onNext(4);
                subscriber.onNext(5);
                Utils.sleep(500);
                subscriber.onNext(6);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnCompleted(() -> System.out.println("observable1-Completed"))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        System.out.println(mList);
        assertEquals(mList, Arrays.asList(1, 2, 4, 6));
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source Observable during
     * sequential
     * time windows of a specified duration.
     * <p/>
     * 根据throttleFirst的官方宝蓝图实现
     */
    @Test
    public void throttleLast() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(500);
                subscriber.onNext(2);
                subscriber.onNext(3);
                Utils.sleep(500);
                subscriber.onNext(4);
                subscriber.onNext(5);
                Utils.sleep(500);
                subscriber.onNext(6);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .doOnCompleted(() -> System.out.println("observable1-Completed"))
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribe(System.out::println);

        mTestScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        System.out.println(mList);
        assertEquals(mList, Arrays.asList(1, 3, 5, 6));
    }

}
