package com.geniusmart.rxjava.subject;

import com.geniusmart.rxjava.utils.OperatorUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static junit.framework.Assert.assertEquals;

/**
 * TODO Subject 是否为hot？
 * Created by geniusmart on 2016/11/18.
 * http://reactivex.io/documentation/subject.html
 */
public class SubjectTest {

    private TestScheduler mTestScheduler;
    private List<Object> mListOne;
    private List<Object> mListTwo;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mListOne = new ArrayList<>();
        mListTwo = new ArrayList<>();
    }

    /**
     * @see <a href="http://reactivex.io/documentation/operators/images/S.BehaviorSubject.png">BehaviorSubject</a>
     */
    @Test
    public void behaviorSubject1() {

        BehaviorSubject<Integer> behaviorSubject = BehaviorSubject.create(1);
        behaviorSubject.subscribe(mListOne::add);

        behaviorSubject.onNext(2);
        behaviorSubject.onNext(3);

        behaviorSubject.subscribe(mListTwo::add);

        behaviorSubject.onNext(4);

        assertEquals(mListOne, Arrays.asList(1, 2, 3, 4));
        assertEquals(mListTwo, Arrays.asList(3, 4));

    }

    /**
     * @see <a href="http://reactivex.io/documentation/operators/images/S.BehaviorSubject.e.png">BehaviorSubject</a>
     */
    @Test
    public void behaviorSubject2() {

        BehaviorSubject<Integer> behaviorSubject = BehaviorSubject.create(1);

        behaviorSubject.subscribe(mListOne::add,
                throwable -> {
                    mListOne.add("error");
                });

        behaviorSubject.onNext(2);
        behaviorSubject.onError(new Throwable());

        behaviorSubject.subscribe(mListTwo::add,
                throwable -> {
                    mListTwo.add("error");
                });

        assertEquals(mListOne,Arrays.asList(1,2,"error"));
        assertEquals(mListTwo, Collections.singletonList("error"));

    }

    /**
     * @see <a href="http://reactivex.io/documentation/operators/images/S.PublishSubject.png">PublishSubject</a>
     */
    @Test
    public void publishSubject1(){

        PublishSubject<Integer> publishSubject = PublishSubject.create();

        publishSubject.subscribe(mListOne::add);

        //延迟500ms订阅
        publishSubject.delaySubscription(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe(mListTwo::add);

        publishSubject.onNext(1);
        publishSubject.onNext(2);

        //延迟1000ms发送数据
        OperatorUtils.sleep(1000);

        publishSubject.onNext(3);
        publishSubject.onCompleted();

        assertEquals(mListOne,Arrays.asList(1,2,3));
        assertEquals(mListTwo, Collections.singletonList(3));
    }

    /**
     * @see <a href="http://reactivex.io/documentation/operators/images/S.PublishSubject.e.png">PublishSubject</a>
     */
    @Test
    public void publicSubject2(){

        PublishSubject<Integer> publishSubject = PublishSubject.create();

        publishSubject.subscribe(mListOne::add,throwable -> {
            mListOne.add("error");
        });

        //延迟500ms订阅
        publishSubject.delaySubscription(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe(mListTwo::add,throwable -> {
                    mListTwo.add("error");
                });

        publishSubject.onNext(1);
        publishSubject.onNext(2);

        //延迟1000ms发送数据
        OperatorUtils.sleep(1000);
        publishSubject.onError(new Throwable());

        assertEquals(mListOne,Arrays.asList(1,2,"error"));
        assertEquals(mListTwo, Collections.singletonList("error"));
    }
}
