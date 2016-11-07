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

        assertEquals(mList, Arrays.asList(1,2,1,2,1,2));
    }

    @Test
    public void retryWhen(){
        Observable.create((Subscriber<? super String> s) -> {

            int i = 0;
            System.out.println("subscribing");
            System.out.println(5/i++);
            //s.onError(new RuntimeException("always fails"));
        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return observable.zipWith(Observable.range(1, 3), new Func2<Throwable, Integer, Integer>() {
                    @Override
                    public Integer call(Throwable throwable, Integer integer) {
                        System.out.println(throwable.getMessage());
                        return integer;
                    }
                }).flatMap(new Func1<Integer, Observable<?>>() {
                    @Override
                    public Observable<?> call(Integer o) {
                        System.out.println("delay retry by " + o + " second(s)");
                        return Observable.timer(o,TimeUnit.SECONDS);
                    }
                });
            }
        }).toBlocking().forEach(System.out::println);
    }

}
