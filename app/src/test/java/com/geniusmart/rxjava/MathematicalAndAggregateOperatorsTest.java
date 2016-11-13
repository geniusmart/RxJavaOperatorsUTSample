package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observables.MathObservable;
import rx.schedulers.TestScheduler;

/**
 * Created by geniusmart on 16/11/2.
 * <p>
 * Operators that operate on the entire sequence of items emitted by an Observable
 */
public class MathematicalAndAggregateOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    @Test
    public void average(){
        Observable<Integer> observable = Observable.just(1, 2, 2, 2, 5);
        MathObservable.averageInteger(observable)
                .subscribe(System.out::println);

        MathObservable.averageDouble(Observable.just(1.0, 2.0, 2.0, 2.0, 5.0))
                .subscribe(System.out::println);
    }

    @Test
    public void concat() {
        Observable<Integer> observable1 = Observable.interval(10, TimeUnit.SECONDS, mTestScheduler)
                .map(aLong -> 1)
                .take(3);

        Observable<Integer> observable2 = Observable.interval(1, TimeUnit.SECONDS, mTestScheduler)
                .map(aLong -> 2)
                .take(2);

        Observable.concat(observable1, observable2)
                .subscribe(mList::add);
        advanceTimeAndPrint(100);
    }

    @Test
    public void count(){
        Observable.just(1,2,3,4).count()
                .subscribe(System.out::println);
    }

    @Test
    public void max(){
        MathObservable.max(Observable.just(2,30,22,5,60,1))
                .subscribe(System.out::println);
    }

    @Test
    public void min(){
        MathObservable.min(Observable.just(2,30,22,5,60,1))
                .subscribe(System.out::println);
    }

    @Test
    public void reduce(){
       Observable.just(1,2,3,4,5)
               .reduce((num1, num2) -> num1 + num2)
               .subscribe(System.out::println);
    }

    @Test
    public void sum(){
        MathObservable.sumInteger(Observable.just(1,2,3,4,5))
                .subscribe(System.out::println);
    }

    private void advanceTimeAndPrint(long delayTime) {
        mTestScheduler.advanceTimeBy(delayTime, TimeUnit.SECONDS);
        System.out.println(mList);
    }
}
