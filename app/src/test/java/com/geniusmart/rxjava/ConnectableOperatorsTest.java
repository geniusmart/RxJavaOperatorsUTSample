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
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by geniusmart on 16/11/6.
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
     * TODO-instruct a connectable Observable to begin emitting items to its subscribers
     * TODO-doOnNext在什么线程
     */
    @Test
    public void connect() {

        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        List<Integer> list3 = new ArrayList<>();

        //构造1,2,3的数据流，每隔3s发射数据
        ConnectableObservable<Integer> publish = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                //subscriber.onNext(System.currentTimeMillis());
                subscriber.onNext(1);
                Utils.sleep(3000);
                //subscriber.onNext(System.currentTimeMillis());
                subscriber.onNext(2);
                Utils.sleep(3000);
                //subscriber.onNext(System.currentTimeMillis());
                subscriber.onNext(3);
            }
        }).publish();

        System.out.println("Subscriber1 开始订阅数据"+System.currentTimeMillis());
        //立刻订阅完整的数据流
        publish.doOnNext(num -> System.out.println("Subscriber1-->" + num))
                .subscribe(list1::add);

        //延迟6s后再订阅，将只订阅到3的数据流
        Observable.timer(6,TimeUnit.SECONDS, Schedulers.newThread())
                .map((Func1<Long, Object>) aLong -> {
                    System.out.println("Subscriber2 开始订阅数据"+System.currentTimeMillis());
                    publish.doOnNext(num -> System.out.println("Subscriber2-->" + num))
                            .subscribe(list2::add);
                    return Observable.empty();
                })
                .subscribe();

        //延迟1s后再订阅，将只订阅到3的数据流
        Observable.timer(1,TimeUnit.SECONDS, Schedulers.newThread())
                .map((Func1<Long, Object>) aLong -> {
                    System.out.println("Subscriber3 开始订阅数据"+System.currentTimeMillis()+Thread.currentThread().getName());
                    publish.doOnNext(num -> System.out.println("Subscriber3-->" + num))
                            .subscribe(list3::add);
                    return Observable.empty();
                })
                .subscribe();




        //延时2s执行connect()
        Utils.sleep(2000);
        System.out.println("connect"+System.currentTimeMillis());
        publish.connect();
//        Observable.timer(2,TimeUnit.SECONDS, mTestScheduler)
//                .map((Func1<Long, Object>) aLong -> {
//                    System.out.println("connect"+System.currentTimeMillis());
//                    publish.connect();
//                    return Observable.empty();
//                })
//                .subscribe();


        //mTestScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        assertEquals(list1, Arrays.asList(1, 2, 3));
        assertEquals(list2, Collections.singletonList(3));
        assertEquals(list3, Arrays.asList(1, 2, 3));
    }

    @Test
    public void refCount() {

    }

    @Test
    public void replay() {

    }
}
