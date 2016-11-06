package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by geniusmart on 16/11/6.
 * A toolbox of useful Operators for working with Observables
 */
public class UtilityOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    @Test
    public void delay() {
        Observable.just(1, 2, 1)
                .delay(3000, TimeUnit.SECONDS, mTestScheduler)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(2000, TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
        assertTrue(mList.isEmpty());

        mTestScheduler.advanceTimeBy(3000, TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
        assertEquals(mList, Arrays.asList(1, 2, 1));

    }

    //TODO：RxJs的操作符
    @Test
    public void delayWithSelector() {

        Observable.just(1, 2, 1)
                .delay(new Func1<Integer, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Integer integer) {
                        return Observable.timer(integer * 20, TimeUnit.SECONDS);
                    }
                }).subscribe(mList::add);
        mTestScheduler.advanceTimeBy(3000, TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
    }

    @Test
    public void doOperator(){

    }

    @Test
    public void materialize(){

    }

    @Test
    public void dematerialize(){

    }

    @Test
    public void observeOn(){

    }

    @Test
    public void serialize(){

    }

    @Test
    public void subscribe(){

    }

    @Test
    public void subscribeOn(){

    }

    @Test
    public void timeInterval(){

    }

    @Test
    public void timeout(){

    }

    @Test
    public void timestamp(){

    }

    @Test
    public void using(){

    }
}
