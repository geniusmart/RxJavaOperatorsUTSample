package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.TestScheduler;

/**
 * Created by geniusmart on 2016/11/2.
 */
public class FilteringOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

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

    //TODO 可以作为范例
    /**
     * SkipUntil — discard items emitted by an Observable until a second Observable emits an item
     */
    @Test
    public void skipUntil(){

        Observable<Long> o1 = Observable.interval(100, TimeUnit.SECONDS, mTestScheduler)
                .map(num->num+1)
                .take(9)
                .doOnNext(System.out::println);

        Observable<Integer> o2 = Observable.just(0, 0).delay(550, TimeUnit.SECONDS,mTestScheduler);

        o1.skipUntil(o2)
                .subscribe(mList::add);

        advanceTimeAndPrint(2000);
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

    private void advanceTimeAndPrint(long delayTime) {
        mTestScheduler.advanceTimeBy(delayTime, TimeUnit.SECONDS);
        System.out.println(mList);
    }
}
