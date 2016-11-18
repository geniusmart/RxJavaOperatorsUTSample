package com.geniusmart.rxjava.subject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.schedulers.TestScheduler;
import rx.subjects.ReplaySubject;

/**
 * Created by geniusmart on 2016/11/18.
 * http://reactivex.io/documentation/subject.html
 */
public class SubjectTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    @Test
    public void ReplaySubject(){
        ReplaySubject<Integer> replaySubject = ReplaySubject.create();
        replaySubject.onNext(1);
        replaySubject.onNext(2);
        replaySubject.onNext(3);
        replaySubject.onCompleted();

        replaySubject.subscribe();
    }
}
