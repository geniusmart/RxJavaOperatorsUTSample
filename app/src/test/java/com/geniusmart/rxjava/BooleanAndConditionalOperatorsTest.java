package com.geniusmart.rxjava;

import org.junit.Test;

import rx.Observable;

import static android.R.attr.x;

/**
 * Created by geniusmart on 16/11/2.
 */

public class BooleanAndConditionalOperatorsTest {

    //RxJs的every操作符相当于RxJava的all
    @Test
    public void every(){
        Observable.just(1,2,3,4,5)
                .all( x -> x<10)
                .subscribe(System.out::println);
    }

    //TODO
    @Test
    public void some(){
        Observable.just(2,30,22,5,60,1)
                .exists(integer -> x>100)
                .subscribe(System.out::println);
    }

    //TODO-哪里来的includes？？
    @Test
    public void includes(){
        Observable.just(2,30,22,5,60,1)
                .contains(22)
                .subscribe(System.out::println);
    }

    //TODO--加深理解
    @Test
    public void sequenceEqual(){
        Observable.sequenceEqual(Observable.just(1,2,3),Observable.just(1,2,3))
                .subscribe(System.out::println);
    }

    @Test
    public void amb(){
        
    }

}
