package com.geniusmart.rxjava;

import com.geniusmart.rxjava.utils.OperatorUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

/**
 * Created by geniusmart on 16/11/6.
 * <p>
 * strategies for coping with Observables that produce items more rapidly than their observers
 * consume them
 */
public class BackpressureOperatorsTest {

    //TODO 结合http://rxmarbles.com/#pausable

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    @Test
    public void doOnRequest() {
        Observable.range(0, 300)
                .doOnRequest(i -> System.out.println(this.toString() + "--Requested " + i))
                .zipWith(
                        Observable.range(10, 300),
                        (i1, i2) -> i1 + " - " + i2)
                .take(300)
                .subscribe();
    }

    @Test
    public void onBackpressureBuffer() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer(1000)
                .observeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    System.out.println(aLong);
                    OperatorUtils.sleep(100);
                });

        OperatorUtils.sleep(2000);
    }

    //http://blog.chengyunfeng.com/?p=981
    @Test
    public void onBackpressureDrop() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(Schedulers.newThread())
                .subscribe(
                        i -> {
                            System.out.println(i);
                            try {
                                Thread.sleep(10);
                            } catch (Exception e) {
                            }
                        },
                        System.out::println);
        OperatorUtils.sleep(50000);
    }

}
