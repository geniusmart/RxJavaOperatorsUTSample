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
import static junit.framework.Assert.assertTrue;

/**
 * Created by geniusmart on 16/11/6.
 * <p>
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

    /**
     * shift the emissions from an Observable forward in time by a particular amount
     * <p>
     * 此例子根据RxMarbles进行实现
     * @see <a href="http://rxmarbles.com/#delay">delay diagrams
     */
    @Test
    public void delay() {
        Observable.just(1, 2, 1)
                .delay(3000, TimeUnit.SECONDS, mTestScheduler)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(2000, TimeUnit.SECONDS);
        System.out.println("after 2000ms,result = " + mList);
        assertTrue(mList.isEmpty());

        mTestScheduler.advanceTimeBy(1000, TimeUnit.SECONDS);
        System.out.println("after 3000ms,result = " + mList);
        assertEquals(mList, Arrays.asList(1, 2, 1));

    }

    /**
     * shift the emissions from an Observable forward in time by a particular amount
     * <p>
     * 此例子根据RxMarbles进行实现
     * @see <a href="http://rxmarbles.com/#delayWithSelector">delayWithSelector diagrams
     */
    @Test
    public void delayWithSelector() {

        Observable.just(1, 2, 1)
                .delay(integer -> Observable.timer(integer * 20, TimeUnit.SECONDS, mTestScheduler))
                .subscribe(mList::add);
        mTestScheduler.advanceTimeBy(3000, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(1, 1, 2));
    }

    @Test
    public void delaySubscription() {

        //延时5s订阅
        Observable.just(888)
                .delaySubscription(5, TimeUnit.SECONDS, mTestScheduler)
                .doOnSubscribe(() -> System.out.println("o1->doOnSubscribe"))
                .doOnNext(System.out::println)
                .subscribe(mList::add);

        //延时2s订阅，此数据流会先被订阅
        Observable.just(666)
                .delaySubscription(2, TimeUnit.SECONDS, mTestScheduler)
                .doOnSubscribe(() -> System.out.println("o2->doOnSubscribe"))
                .doOnNext(System.out::println)
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(10, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(666, 888));
    }

    @Test
    public void doOperator() {

    }

    @Test
    public void materialize() {

    }

    @Test
    public void dematerialize() {

    }

    @Test
    public void observeOn() {

    }

    @Test
    public void serialize() {

    }

    @Test
    public void subscribe() {

    }

    @Test
    public void subscribeOn() {

    }

    @Test
    public void timeInterval() {

    }

    @Test
    public void timeout() {

    }

    @Test
    public void timestamp() {

    }

    @Test
    public void using() {

    }
}
