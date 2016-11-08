package com.geniusmart.rxjava;

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
import rx.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by geniusmart on 16/11/6.
 * Operators that help to recover from error notifications from an Observable
 */
public class ErrorHandlingOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    //TODO
    @Test
    public void catchOperator() {

    }

    @Test
    public void retry() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(2 / 0);
                subscriber.onCompleted();
            }
        })
                .retry(2)
                .subscribe(integer -> {
                    System.out.println(integer);
                    mList.add(integer);
                }, throwable -> System.out.println(throwable.getMessage()));

        assertEquals(mList, Arrays.asList(1, 2, 1, 2, 1, 2));
    }

    /**
     * 破坏数据流
     * 有关repeatWhen的参考文章：http://www.jianshu.com/p/023a5f60e6d0
     */
    @Test
    public void retryWhen_break_sequence() {

        // 错误的做法：破坏数据流,打断链式结构
        Observable.just(1, 2, 3)
                .retryWhen(throwableObservable -> Observable.just(1, 1, 1))
                .subscribe(mList::add);
        //数据流被打断，订阅不到数据
        assertTrue(mList.isEmpty());

        // 正确的做法：至少将throwableObservable作为返回结果，此时的retryWhen()等价于retry()
        Observable.just(1, 2, 3)
                .retryWhen(throwableObservable -> throwableObservable).
                subscribe(mList::add);
        //此处的数据流不会触发error，因此正常输出1,2,3的数列
        assertEquals(mList, Arrays.asList(1, 2, 3));
    }

    /**
     * 使用retryWhen() + flatMap() + timer() 实现延迟重新订阅
     */
    @Test
    public void retryWhen_flatMap_timer() {

        Observable.create(subscriber -> {
            System.out.println("subscribing");
            subscriber.onNext(1);
            subscriber.onNext(2);
            subscriber.onError(new RuntimeException("RuntimeException"));
        })
                .retryWhen(observable ->
                        observable.flatMap(
                                (Func1<Throwable, Observable<?>>) throwable ->
                                        //延迟5s重新订阅
                                        Observable.timer(5, TimeUnit.SECONDS, mTestScheduler)
                        )
                )
                .subscribe(num -> {
                    System.out.println(num);
                    mList.add(num);
                });

        //时间提前10s，将发生1次订阅+2次重新订阅
        mTestScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        assertEquals(mList, Arrays.asList(1, 2, 1, 2, 1, 2));
    }

    /**
     * TODO：使用zip() + range()实现有限次数的重订阅
     */
    @Test
    public void retryWhen_zip_range() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("subscribing");
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onError(new RuntimeException("always fails"));
            }
        })
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> throwable) {
                        return throwable.zipWith(Observable.range(1, 3), new Func2<Throwable, Integer, Object>() {
                            @Override
                            public Object call(Throwable throwable, Integer integer) {
                                System.out.println("--->" + integer);
                                return integer;
                            }
                        });
                    }
                })
                .doOnNext(System.out::println)
                .doOnCompleted(() -> System.out.println("completed"))
                .subscribe(mList::add);

        assertEquals(mList, Arrays.asList(1, 2, 1, 2, 1, 2, 1, 2));
    }

    /**
     * 延迟策略与次数限制的重试机制结合起来
     */
    @Test
    public void retryWhen_zip_range_timer() {

        Observable.create((Subscriber<? super Integer> subscriber) -> {
            System.out.println("subscribing");
            subscriber.onNext(1);
            subscriber.onNext(2);
            subscriber.onError(new RuntimeException("always fails"));
        })
                .doOnNext(System.out::println)
                .retryWhen(observable ->
                        observable.zipWith(
                                Observable.range(1, 3),
                                (Func2<Throwable, Integer, Integer>) (throwable, integer) -> integer
                        )
                                .flatMap((Func1<Integer, Observable<?>>) num -> {
                                    System.out.println("delay retry by " + num + " second(s)");
                                    return Observable.timer(num, TimeUnit.SECONDS);
                                }))
                .toBlocking()
                .forEach(mList::add);

        //订阅一次，重新订阅3次
        assertEquals(mList, Arrays.asList(1, 2, 1, 2, 1, 2, 1, 2));
    }

}
