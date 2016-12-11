package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.TestScheduler;
import rx.util.async.Async;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

/**
 * Created by geniusmart on 2016/11/4.
 * <p>
 * Operators that originate new Observables.
 */
public class CreatingOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * create an Observable from scratch by calling observer methods programmatically
     *
     * @see <a href="http://reactivex.io/documentation/operators/create.html">
     * ReactiveX operators documentation: Create</a>
     */
    @Test
    public void create() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(3);
                subscriber.onNext(5);
                subscriber.onError(new ClassCastException());
                subscriber.onNext(7);
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getClass());
            }

            @Override
            public void onNext(Integer integer) {
                mList.add(integer);
            }
        });

        assertEquals(mList, Arrays.asList(1, 3, 5));

    }

    /**
     * do not create the Observable until the observer subscribes, and create a fresh Observable
     * for each observer
     * <p/>
     * 此例子用于说明 defer 产生的数据流都是全新的
     *
     * @see <a href="http://reactivex.io/documentation/operators/defer.html">ReactiveX operators
     * documentation: Defer</a>
     */
    @Test
    public void defer1() {

        List<Observable<Integer>> list = new ArrayList<>();

        Observable<Integer> deferObservable = Observable.defer(() -> {
            Observable<Integer> observable = Observable.just(1, 2, 3);
            list.add(observable);
            return observable;
        });

        // 两次订阅，每次都将产生全新的Observable
        deferObservable.subscribe();
        deferObservable.subscribe();

        assertNotSame(list.get(0), list.get(1));
    }

    /**
     * do not create the Observable until the observer subscribes, and create a fresh Observable
     * for each observer
     * <p/>
     * 此例子用于说明defer延迟订阅的作用
     *
     * @see <a href="http://reactivex.io/documentation/operators/defer.html">ReactiveX operators
     * documentation: Defer</a>
     * @see <a href="http://www.jianshu.com/p/c83996149f5b">【译】使用RxJava实现延迟订阅</a>
     */
    @Test
    public void defer2() {

        class Person {
            public String name = "nobody";

            public Observable<String> getJustObservable() {
                //创建的时候便获取name值
                return Observable.just(name);
            }

            public Observable<String> getDeferObservable() {
                //订阅的时候才获取name值
                return Observable.defer(this::getJustObservable);
            }
        }

        Person person = new Person();
        Observable<String> justObservable = person.getJustObservable();
        Observable<String> deferObservable = person.getDeferObservable();

        // 数据改变之前
        justObservable.subscribe(mList::add);
        assertEquals(mList, Collections.singletonList("nobody"));

        mList.clear();
        deferObservable.subscribe(mList::add);
        assertEquals(mList, Collections.singletonList("nobody"));

        person.name = "geniusmart";

        // 数据改变之后
        mList.clear();
        justObservable.subscribe(mList::add);
        assertEquals(mList, Collections.singletonList("nobody"));

        mList.clear();
        deferObservable.subscribe(mList::add);
        assertEquals(mList, Collections.singletonList("geniusmart"));

    }

    /**
     * create an Observable that emits no items but terminates normally
     *
     * @see <a href="http://reactivex.io/documentation/operators/empty-never-throw.html">ReactiveX
     * operators documentation: Empty</a>
     */
    @Test
    public void empty() {
        Observable.empty()
                .doOnNext(value -> mList.add(value))
                .doOnCompleted(() -> mList.add("completed"))
                .subscribe(mList::add);

        assertEquals(mList, Collections.singletonList("completed"));
    }

    /**
     * create an Observable that emits no items and does not terminate
     *
     * @see <a href="http://reactivex.io/documentation/operators/empty-never-throw.html">ReactiveX
     * operators documentation: Empty</a>
     */
    @Test
    public void never() {
        Observable.never()
                .doOnNext(value -> mList.add(value))
                .doOnCompleted(() -> mList.add("completed"))
                .subscribe(mList::add);
        assertTrue(mList.isEmpty());
    }

    /**
     * create an Observable that emits no items and terminates with an error
     *
     * @see <a href="http://reactivex.io/documentation/operators/empty-never-throw.html">ReactiveX
     * operators documentation: Empty</a>
     */
    @Test
    public void error() {
        Observable.error(new NullPointerException())
                .subscribe(
                        value -> mList.add(value),
                        throwable -> mList.add("error"),
                        () -> mList.add("completed")
                );

        assertEquals(mList, Collections.singletonList("error"));
    }

    /**
     * convert some other object or data structure into an Observable
     *
     * @see <a href="http://reactivex.io/documentation/operators/from.html">ReactiveX operators
     * documentation: From</a>
     */
    @Test
    public void from() {
        String[] values = new String[]{"a", "b", "c"};
        Observable.from(values)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList("a", "b", "c"));
    }

    /**
     * create an Observable that emits a sequence of integers spaced by a particular time interval
     *
     * @see <a href="http://reactivex.io/documentation/operators/interval.html">ReactiveX operators
     * documentation: Interval</a>
     */
    @Test
    public void interval() {
        Observable.interval(100, TimeUnit.MILLISECONDS, mTestScheduler)
                .subscribe(mList::add);

        //时间提早400ms前
        mTestScheduler.advanceTimeBy(400, TimeUnit.MILLISECONDS);
        assertEquals(mList, Arrays.asList(0L, 1L, 2L, 3L));

        //时间提早(400 + 200)ms前
        mTestScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
        assertEquals(mList, Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L));
    }

    /**
     * convert an object or a set of objects into an Observable that emits that or those objects
     *
     * @see <a href="http://reactivex.io/documentation/operators/just.html">ReactiveX operators
     * documentation: Just</a>
     */
    @Test
    public void just() {
        Observable.just(1, 2, 3, 4, 5)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 3, 4, 5));
    }

    /**
     * create an Observable that emits a range of sequential integers
     *
     * @see <a href="http://reactivex.io/documentation/operators/range.html">ReactiveX operators
     * documentation: Range</a>
     */
    @Test
    public void range() {
        Observable.range(2, 6)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(2, 3, 4, 5, 6, 7));
    }

    /**
     * create an Observable that emits a particular item or sequence of items repeatedly
     *
     * @see <a href="http://reactivex.io/documentation/operators/repeat.html">ReactiveX operators
     * documentation: Repeat</a>
     */
    @Test
    public void repeat() {
        Observable.just(1, 2)
                .repeat(3)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 1, 2, 1, 2));
    }

    /**
     * 实现一个延迟数秒的重订阅
     *
     * @see <a href="http://reactivex.io/documentation/operators/repeat.html">ReactiveX operators
     * documentation: Repeat</a>
     * @see <a href="http://reactivex.io/documentation/operators/images/repeatWhen.f.png">repeatWhen.png</a>
     * @see <a href="http://www.jianshu.com/p/023a5f60e6d0">repeatWhen()和retryWhen()</a>
     */
    @Test
    public void repeatWhen() {

        Observable.just(1, 2)
                .repeatWhen(completed -> completed.delay(5, TimeUnit.SECONDS, mTestScheduler))
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(3, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(1, 2));

        mTestScheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(1, 2, 1, 2));

    }

    /**
     * create an Observable that emits the return value of a function
     *
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Async-Operators#wiki-start">
     * RxJava Wiki: start()</a>
     */
    @Test
    public void start() {
        //只执行一次
        Observable<Integer> startObservable = Async.start(() -> 1);
        startObservable.subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(1));

        mList.clear();
        startObservable.subscribe(System.out::println);
        assertTrue(mList.isEmpty());
    }

    /**
     * create an Observable that emits a single item after a given delay
     *
     * @see <a href="http://reactivex.io/documentation/operators/timer.html">ReactiveX operators
     * documentation: Timer</a>
     */
    @Test
    public void timer() {

        //仅发射0的数值，延迟100s
        Observable.timer(100, TimeUnit.SECONDS, mTestScheduler)
                .subscribe(mList::add);

        assertTrue(mList.isEmpty());

        mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);
        assertEquals(mList, Collections.singletonList(0L));
    }

}
