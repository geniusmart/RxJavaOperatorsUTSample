package com.geniusmart.rxjava;

import org.junit.Test;

import rx.Observable;

/**
 * Created by geniusmart on 2016/11/2.
 */
public class FilteringOperatorsTest {

    @Test
    public void distinct(){
        Observable.just(1,2,2,1,3)
                .distinct()
                .subscribe(System.out::println);
    }

    @Test
    public void distinctUtilChange(){
        Observable.just(1,2,2,1,3)
                .distinctUntilChanged()
                .subscribe(System.out::println);
    }

    @Test
    public void elementAt(){
        Observable.just(1,2,3,4)
                .elementAt(2)
                .subscribe(System.out::println);
    }

    @Test
    public void filter(){
        Observable.just(2,30,22,5,60,1)
                .filter(integer -> integer > 10)
                .subscribe(System.out::println);
    }

    /**
     * find为RxPY操作符，这里使用filter+first来实现
     * http://rxmarbles.com/#find
     */
    @Test
    public void find(){
        Observable.just(2,30,22,5,60,1)
                .filter(integer -> integer > 10)
                .first()
                .subscribe(System.out::println);
    }

    @Test
    public void first(){
        Observable.just(1,2,3,4)
                .first()
                .subscribe(System.out::println);
    }

    @Test
    public void last(){
        Observable.just(1,2,3,4)
                .last()
                .subscribe(System.out::println);
    }

    //TODO-此为RxJs的操作符
    @Test
    public void pausable(){
        Observable.just(1);
    }

    //TODO
    @Test
    public void pausableBuffered(){

    }

    @Test
    public void skip(){
        Observable.just(1,2,3,4)
                .skip(2)
                .subscribe(System.out::println);
    }

    @Test
    public void skipLast(){
        Observable.just(1,2,3,4)
                .skipLast(2)
                .subscribe(System.out::println);
    }

    //TODO 什么鸟东西？
    @Test
    public void skipUntil(){
        Observable.just(1,2,3,4,5,6,7,8,9)
                .skipUntil(Observable.just(0,0))
                .subscribe(System.out::println);
    }

    @Test
    public void take(){
        Observable.just(1,2,3,4)
                .take(2)
                .subscribe(System.out::println);
    }

    @Test
    public void takeLast(){
        Observable.just(1,2,3,4)
                .takeLast(1)
                .subscribe(System.out::println);
    }

    //TODO 此处的2个重载方法如何使用？？？
    @Test
    public void takeUntil(){
        Observable.just(1,2,3,4)
                .takeUntil(integer -> integer < 6)
                .subscribe(System.out::println);
    }

}
