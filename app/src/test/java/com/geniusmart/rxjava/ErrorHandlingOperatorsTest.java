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
 * <p>
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

    /**
     * instructs an Observable to emit a particular item when it encounters an error, and then
     * terminate normally
     * <p>
     * 根据官方 marble diagrams 实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/catch.html">ReactiveX operators
     * documentation: Catch</a>
     */
    @Test
    public void onErrorReturn() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("○");
                subscriber.onNext("○");
                subscriber.onNext("○");
                subscriber.onError(new ArithmeticException());
            }
        })
                .onErrorReturn(throwable -> "◇")
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList("○", "○", "○", "◇"));
    }

    /**
     * instructs an Observable to begin emitting a second Observable sequence if it encounters an
     * error
     * <p>
     * 根据官方 marble diagrams 实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/catch.html">ReactiveX operators
     * documentation: Catch</a>
     */
    @Test
    public void onErrorResumeNext() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
                subscriber.onError(new NullPointerException());
            }
        })
                .onErrorResumeNext(Observable.just(4, 5))
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 3, 4, 5));
    }

    /**
     * instructs an Observable to begin emitting a second Observable sequence if it encounters an
     * error
     */
    @Test
    public void onErrorResumeNext2() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onError(new NullPointerException());
            }
        }).onErrorResumeNext(throwable -> {
            if (throwable instanceof NullPointerException) {
                return Observable.just(3, 4);
            }
            return Observable.just(5, 6);
        }).subscribe(mList::add);

        assertEquals(mList, Arrays.asList(1, 2, 3, 4));
    }

    /**
     * instructs an Observable to continue emitting items after it encounters an exception (but not
     * another variety of throwable)
     *
     * @see <a href="http://reactivex.io/documentation/operators/catch.html">ReactiveX operators
     * documentation: Catch</a>
     */
    @Test
    public void onExceptionResumeNext() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onError(new Throwable("throwable"));
            }
        })
                .onExceptionResumeNext(Observable.just(4))
                .subscribe(
                        mList::add,
                        throwable -> System.out.println(throwable.getMessage())
                );

        //onExceptionResumeNext只处理Exception类型的error，其他类型(如Error和Throwable)的异常不进行处理
        assertEquals(mList, Arrays.asList(1, 2));
    }

    /**
     * recover from an onError notification by continuing the sequence without error
     *
     * @see <a href="http://reactivex.io/documentation/operators/retry.html">ReactiveX operators
     * documentation: Retry</a>
     */
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
     * retryWhen破坏数据流的示例
     *
     * @see <a href="http://reactivex.io/documentation/operators/retry.html">ReactiveX operators
     * documentation: Retry</a>
     * @see <a href="http://www.jianshu.com/p/023a5f60e6d0">retryWhen和repeatWhen</a>
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
     *
     * @see <a href="http://reactivex.io/documentation/operators/retry.html">ReactiveX operators
     * documentation: Retry</a>
     * @see <a href="http://www.jianshu.com/p/023a5f60e6d0">retryWhen和repeatWhen</a>
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
     * 延迟策略与次数限制的重试机制结合起来
     *
     * @see <a href="http://reactivex.io/documentation/operators/retry.html">ReactiveX operators
     * documentation: Retry</a>
     * @see <a href="http://www.jianshu.com/p/023a5f60e6d0">retryWhen和repeatWhen</a>
     */
    @Test
    public void retryWhen_zip_range_timer() {

        Observable.create((Subscriber<? super Integer> subscriber) -> {
            System.out.println("subscribing");
            subscriber.onNext(1);
            subscriber.onNext(2);
            subscriber.onError(new RuntimeException("always fails"));
        })
                .retryWhen(observable ->
                        observable.zipWith(
                                Observable.range(1, 3),
                                (Func2<Throwable, Integer, Integer>) (throwable, num) -> num
                        )
                                .flatMap((Func1<Integer, Observable<?>>) num -> {
                                    System.out.println("delay retry by " + num + " second(s)");
                                    return Observable.timer(num, TimeUnit.SECONDS);
                                }))
                .doOnNext(System.out::println)
                .doOnCompleted(() -> System.out.println("completed"))
                .toBlocking()
                .forEach(mList::add);

        //订阅一次，重新订阅3次
        assertEquals(mList, Arrays.asList(1, 2, 1, 2, 1, 2, 1, 2));
    }

}
