package com.geniusmart.rxjava;

import com.geniusmart.rxjava.utils.OperatorUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by geniusmart on 16/11/6.
 * <p>
 * Specialty Observables that have more precisely-controlled subscription dynamics
 */
public class ConnectableOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * convert an ordinary Observable into a connectable Observable
     *
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">publish</a>
     */
    @Test
    public void publish() {
        //创建ConnectableObservable
        ConnectableObservable<Integer> publish =
                Observable.just(1, 2, 3)
                        .publish();

        //此时并不会马上订阅数据
        publish.subscribe(mList::add);
        assertTrue(mList.isEmpty());

        //开始订阅数据
        publish.connect();
        assertEquals(mList, Arrays.asList(1, 2, 3));
    }

    /**
     * instruct a connectable Observable to begin emitting items to its subscribers
     * <p/>
     * 此例子根据connect的官方 marble diagrams 进行实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/publishConnect.png">connect.png</a>
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">connect</a>
     */
    @Test
    public void connect() {

        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        List<Integer> list3 = new ArrayList<>();

        //构造1,2,3的数据流，每隔3s发射数据
        ConnectableObservable<Integer> connectableObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                OperatorUtils.sleep(3000);
                subscriber.onNext(2);
                OperatorUtils.sleep(3000);
                subscriber.onNext(3);
            }
        }).publish();

        System.out.println("Subscriber1-0s后开始订阅数据");
        //立刻订阅完整的数据流
        connectableObservable.doOnNext(num -> System.out.println("Subscriber1-->" + num))
                .subscribe(list1::add);

        //延迟6s后再订阅，将只订阅到3的数据流
        connectableObservable.delaySubscription(6, TimeUnit.SECONDS, Schedulers.newThread())
                .doOnSubscribe(()->{
                    System.out.println("Subscriber2-6s后开始订阅数据");
                })
                .doOnNext(num -> System.out.println("Subscriber2-->" + num))
                .subscribe(list2::add);

        //延迟1s后再订阅，将只订阅到3的数据流
        connectableObservable.delaySubscription(1, TimeUnit.SECONDS, Schedulers.newThread())
                .doOnSubscribe(()->{
                    System.out.println("Subscriber3-1s后开始订阅数据");
                })
                .doOnNext(num -> System.out.println("Subscriber3-->" + num))
                .subscribe(list3::add);


        //延时2s执行connect()
        OperatorUtils.sleep(2000);
        System.out.println("Observable 2s后触发connect()");
        connectableObservable.connect();

        assertEquals(list1, Arrays.asList(1, 2, 3));
        assertEquals(list2, Collections.singletonList(3));
        assertEquals(list3, Arrays.asList(1, 2, 3));
    }

    /**
     * make a Connectable Observable behave like an ordinary Observable
     * <p/>
     * 此用例介绍refCount的概念，官方 marble diagrams 的实现详见refCount2()用例
     *
     * @see <a href="http://reactivex.io/documentation/operators/refcount.html">ReactiveX
     * documentation: RefCount</a>
     */
    @Test
    public void refCount() {
        //构造1,2,3的数据流，每隔3s发射数据
        ConnectableObservable<Integer> connectableObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
                subscriber.onCompleted();
            }
        }).publish();

        Observable<Integer> observable = connectableObservable.refCount();

        observable.subscribe(mList::add);

        assertEquals(mList, Arrays.asList(1, 2, 3));

    }

    /**
     * make a Connectable Observable behave like an ordinary Observable
     * <p/>
     * 此例子根据connect的官方 marble diagrams 进行实现，refCount将ConnectableObservable转换为普通的Observable，
     * 但仍然保持了hot数据流的特点，可对比下文的without_public_and_refCount()中cold数据流的区别
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/publishRefCount.c.png">RefCount.png</a>
     * @see <a href="http://reactivex.io/documentation/operators/refcount.html">ReactiveX
     * documentation: RefCount</a>
     */
    @Test
    public void refCount2() {

        List<Long> list1 = new ArrayList<>();
        List<Long> list2 = new ArrayList<>();

        //构造0,1,2的数据流，每隔300ms发射数据
        Observable<Long> observable = Observable.interval(0, 300, TimeUnit.MILLISECONDS)
                .take(3)
                .publish()
                .refCount()
                .doOnNext(System.out::println)
                .doOnCompleted(() -> System.out.println("onCompleted"))
                .subscribeOn(Schedulers.newThread());

        Subscription subscribe1 = observable.subscribe(list1::add);
        OperatorUtils.sleep(100);
        Subscription subscribe2 = observable.subscribe(list2::add);

        OperatorUtils.sleep(400);
        subscribe1.unsubscribe();
        subscribe2.unsubscribe();

        OperatorUtils.sleep(600);

        assertEquals(list1, Arrays.asList(0L, 1L));
        assertEquals(list2, Collections.singletonList(1L));

    }

    /**
     * 此方法与refCount2比对，展示hot与cold数据流的区别
     */
    @Test
    public void without_public_and_refCount() {

        List<Long> list1 = new ArrayList<>();
        List<Long> list2 = new ArrayList<>();

        //构造1,2,3的数据流，每隔3s发射数据
        Observable<Long> observable = Observable.interval(0, 300, TimeUnit.MILLISECONDS)
                .take(3)
                .doOnNext(System.out::println)
                .doOnCompleted(() -> System.out.println("onCompleted"))
                .subscribeOn(Schedulers.newThread());

        Subscription subscribe1 = observable.subscribe(list1::add);
        OperatorUtils.sleep(100);
        Subscription subscribe2 = observable.subscribe(list2::add);

        OperatorUtils.sleep(400);
        subscribe1.unsubscribe();
        subscribe2.unsubscribe();

        OperatorUtils.sleep(600);

        assertEquals(list1, Arrays.asList(0L, 1L));
        assertEquals(list2, Arrays.asList(0L, 1L));

    }

    /**
     * ensure that all observers see the same sequence of emitted items, even if they subscribe
     * after the Observable has begun emitting items
     * <p>
     * 此例子根据replay的官方 marble diagrams 进行实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/replay.png">replay.png</a>
     * @see <a href="http://reactivex.io/documentation/operators/replay.html">ReactiveX operators
     * documentation: Replay</a>
     */
    @Test
    public void replay() {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();

        //构造1,2,3的数据流，每隔3s发射数据
        ConnectableObservable<Integer> connectableObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("Observable只执行一次");
                subscriber.onNext(1);
                OperatorUtils.sleep(3000);
                subscriber.onNext(2);
                OperatorUtils.sleep(3000);
                subscriber.onNext(3);
                subscriber.onCompleted();
            }
        }).replay();

        System.out.println("Subscriber1-0s后开始订阅数据");
        //立刻订阅完整的数据流
        connectableObservable.doOnNext(num -> System.out.println("Subscriber1-->" + num))
                .subscribe(list1::add);

        //延迟6s后再订阅，将只订阅到3的数据流
        Observable.timer(6, TimeUnit.SECONDS, Schedulers.newThread())
                .map((Func1<Long, Object>) aLong -> {
                    System.out.println("Subscriber2-6s后开始订阅数据");
                    connectableObservable.doOnNext(num -> System.out.println("Subscriber2-->" + num))
                            .subscribe(list2::add);
                    return Observable.empty();
                })
                .subscribe();

        //延时2s执行connect()
        OperatorUtils.sleep(2000);
        System.out.println("Observable 2s后触发connect()");
        connectableObservable.connect();

        assertEquals(list1, Arrays.asList(1, 2, 3));
        assertEquals(list2, Arrays.asList(1, 2, 3));
    }

    /**
     * remember the sequence of items emitted by the Observable and emit the same sequence to
     * future Subscribers
     * <p>
     * cache操作函数和replay类似，但无需使用ConnectableObservable
     * <p>
     * 此例子实现了该 marble diagrams ：https://raw.githubusercontent.com/wiki/ReactiveX/RxJava/images/rx-operators/cache.png
     */
    @Test
    public void cache() {

        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();

        //构造1,2,3的数据流，每隔3s发射数据
        Observable<Integer> cacheObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("Observable只执行一次");
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
                OperatorUtils.sleep(1000);
                subscriber.onNext(4);
                subscriber.onCompleted();
            }
        }).cache().subscribeOn(Schedulers.newThread());

        cacheObservable.subscribe(list1::add);
        OperatorUtils.sleep(800);
        cacheObservable.subscribe(list2::add);

        //确保所有线程能执行完毕
        OperatorUtils.sleep(2000);
        assertEquals(list1, Arrays.asList(1, 2, 3, 4));
        assertEquals(list2, Arrays.asList(1, 2, 3, 4));
    }
}
