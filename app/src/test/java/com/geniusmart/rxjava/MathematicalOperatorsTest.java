package com.geniusmart.rxjava;

import org.junit.Test;

import rx.Observable;
import rx.observables.MathObservable;

/**
 * Created by geniusmart on 16/11/2.
 */
public class MathematicalOperatorsTest {

    @Test
    public void average(){
        Observable<Integer> observable = Observable.just(1, 2, 2, 2, 5);
        MathObservable.averageInteger(observable)
                .subscribe(System.out::println);

        MathObservable.averageDouble(Observable.just(1.0, 2.0, 2.0, 2.0, 5.0))
                .subscribe(System.out::println);
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
}
