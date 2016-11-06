package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 2016/11/2.
 * Operators that transform items that are emitted by an Observable.
 */
public class TransformingOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    //TODO
    @Test
    public void buffer(){

    }

    //TODO
    @Test
    public void flatMap(){

    }

    //TODO
    @Test
    public void groupBy(){

    }

    @Test
    public void map() {
        Observable.just(1, 2, 3)
                .map(integer -> integer * 10)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(10, 20, 30));
    }

    @Test
    public void scan() {

        Observable.just(1, 2, 3, 4, 5)
                .scan((num1, num2) -> num1 + num2)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 3, 6, 10, 15));

    }

    //TODO
    @Test
    public void window(){

    }

    private void advanceTimeAndPrint(long delayTime) {
        mTestScheduler.advanceTimeBy(delayTime, TimeUnit.SECONDS);
        System.out.println(mList);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
