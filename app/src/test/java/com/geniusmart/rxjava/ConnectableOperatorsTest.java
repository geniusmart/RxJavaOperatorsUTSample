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
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by geniusmart on 16/11/6.
 * <p>
 * Specialty Observables that have more precisely-controlled subscription dynamics
 */
public class ConnectableOperatorsTest {

    private List<Object> mList;

    @Before
    public void setUp() {
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
     * 此例子根据connect的官方宝蓝图进行实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/publishConnect.png">connect</a>
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
                Utils.sleep(3000);
                subscriber.onNext(2);
                Utils.sleep(3000);
                subscriber.onNext(3);
            }
        }).publish();

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

        //延迟1s后再订阅，将只订阅到3的数据流
        Observable.timer(1, TimeUnit.SECONDS, Schedulers.newThread())
                .map((Func1<Long, Object>) aLong -> {
                    System.out.println("Subscriber3-1s后开始订阅数据");
                    connectableObservable.doOnNext(num -> System.out.println("Subscriber3-->" + num))
                            .subscribe(list3::add);
                    return Observable.empty();
                })
                .subscribe();


        //延时2s执行connect()
        Utils.sleep(2000);
        System.out.println("Observable 2s后触发connect()");
        connectableObservable.connect();

        assertEquals(list1, Arrays.asList(1, 2, 3));
        assertEquals(list2, Collections.singletonList(3));
        assertEquals(list3, Arrays.asList(1, 2, 3));
    }

    /**
     * TODO-refCount未完成
     * make a Connectable Observable behave like an ordinary Observable
     * <p/>
     * 此例子根据connect的官方宝蓝图进行实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/publishRefCount.c.png">ReactiveX
     * operators: RefCount</a>
     */
    @Test
    public void refCount() {
        //构造1,2,3的数据流，每隔3s发射数据
        ConnectableObservable<Integer> connectableObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(3000);
                subscriber.onNext(2);
                Utils.sleep(3000);
                subscriber.onNext(3);
            }
        }).publish();

    }

    /**
     * ensure that all observers see the same sequence of emitted items, even if they subscribe
     * after the Observable has begun emitting items
     * <p>
     * 此例子根据replay的官方宝蓝图进行实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/replay.png">ReactiveX
     * operators: replay</a>
     */
    @Test
    public void replay() {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();

        //构造1,2,3的数据流，每隔3s发射数据
        ConnectableObservable<Integer> connectableObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(3000);
                subscriber.onNext(2);
                Utils.sleep(3000);
                subscriber.onNext(3);
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
        Utils.sleep(2000);
        System.out.println("Observable 2s后触发connect()");
        connectableObservable.connect();

        assertEquals(list1, Arrays.asList(1, 2, 3));
        assertEquals(list2, Arrays.asList(1, 2, 3));
    }
}
