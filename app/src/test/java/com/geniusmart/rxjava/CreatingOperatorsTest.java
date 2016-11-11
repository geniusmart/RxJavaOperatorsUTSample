package com.geniusmart.rxjava;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.TestScheduler;
import rx.util.async.Async;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by geniusmart on 2016/11/4.
 */

public class CreatingOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * create an Observable from scratch by calling observer methods programmatically
     *
     * @see <a href="http://reactivex.io/documentation/operators/create.html">
     * ReactiveX operators documentation: Create</a>
     */
    @Test
    public void create() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(3);
                subscriber.onNext(5);
                subscriber.onError(new ClassCastException());
                subscriber.onNext(7);
                subscriber.onCompleted();
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getClass());
            }

            @Override
            public void onNext(Integer integer) {
                mList.add(integer);
            }
        });

        assertEquals(mList, Arrays.asList(1, 3, 5));

    }

    //TODO-冷热启动，以及connection和publish？
    @Test
    public void defer() {

        class Person {
            public String name = "nobody";

            public Observable<String> getJustObservable() {
                return Observable.just(name);
            }

            public Observable<String> getDeferObservable() {
                return Observable.defer(this::getJustObservable);
            }
        }

        Person person = new Person();
        Observable<String> justObservable = person.getJustObservable();
        Observable<String> deferObservable = person.getDeferObservable();

        person.name = "geniusmart";

        justObservable.subscribe(mList::add);

        assertEquals(mList, Collections.singletonList("nobody"));

        mList.clear();
        deferObservable.subscribe(mList::add);
        assertEquals(mList, Collections.singletonList("geniusmart"));

    }

    //TODO-可作为范例
    @Test
    public void empty() {
        //create an Observable that emits no items but terminates normally
        Observable.empty()
                .doOnNext(value -> mList.add(value))
                .doOnCompleted(() -> mList.add("completed"))
                .subscribe(mList::add);

        assertEquals(mList, Collections.singletonList("completed"));
    }

    @Test
    public void never() {
        //create an Observable that emits no items and does not terminate
        Observable.never()
                .doOnNext(value -> mList.add(value))
                .doOnCompleted(() -> mList.add("completed"))
                .subscribe(mList::add);
        assertTrue(mList.isEmpty());
    }

    @Test
    public void error() {
        //create an Observable that emits no items and terminates with an error
        Observable.error(new NullPointerException())
                .subscribe(
                        value -> mList.add(value),
                        throwable -> mList.add("error"),
                        () -> mList.add("completed")
                );

        assertEquals(mList, Collections.singletonList("error"));
    }

    @Test
    public void from() {
        String[] values = new String[]{"a", "b", "c"};
        Observable.from(values)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList("a", "b", "c"));
    }

    @Test
    public void interval() {
        Observable.interval(100, TimeUnit.MILLISECONDS, mTestScheduler)
                .subscribe(mList::add);

        //时间提早500ms前
        mTestScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        assertEquals(mList, Arrays.asList(0L, 1L, 2L, 3L, 4L));

        //时间提早(500 + 100)ms前
        mTestScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);
        assertEquals(mList, Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L));
    }

    @Test
    public void just() {
        Observable.just(1, 2, 3, 4, 5)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void range() {
        Observable.range(2, 6)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(2, 3, 4, 5, 6, 7));
    }

    @Test
    public void repeat() {
        Observable.just(1, 2)
                .repeat(3)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2, 1, 2, 1, 2));
    }

    //TODO-如何理解
    @Test
    public void repeatWhen() {
//        Observable.just(1)
//                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(Observable<? extends Void> observable) {
//                        return null;
//                    }
//                })
    }

    //TODO-更多关于rxjava-async-util的操作符
    @Test
    public void start() {
        //只执行一次
        Observable<Integer> startObservable = Async.start(() -> 1);
        startObservable.subscribe(mList::add);
        assertEquals(mList, Collections.singletonList(1));

        mList.clear();
        startObservable.subscribe(System.out::println);
        assertTrue(mList.isEmpty());
    }

    @Test
    public void timer() {

        //仅发射0的数值，延迟100s
        Observable.timer(100, TimeUnit.SECONDS, mTestScheduler)
                .subscribe(mList::add);

        assertTrue(mList.isEmpty());

        mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);
        assertEquals(mList, Collections.singletonList(0L));
    }

}
