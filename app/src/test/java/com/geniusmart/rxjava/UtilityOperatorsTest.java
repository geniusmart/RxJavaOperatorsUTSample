package com.geniusmart.rxjava;

import com.geniusmart.rxjava.utils.Utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
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
     *
     * @see <a href="http://rxmarbles.com/#delay">delay diagrams
     * @see <a href="http://reactivex.io/documentation/operators/delay.html">ReactiveX operators
     * documentation: Delay</a>
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
     *
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

    /**
     * Returns an Observable that delays the subscription to the source Observable by a given
     * amount of time.
     *
     * @see <a href="http://reactivex.io/documentation/operators/delay.html">ReactiveX operators
     * documentation: Delay</a>
     */
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

    /**
     * register an action to take upon a variety of Observable lifecycle events
     *
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators
     * documentation: Do</a>
     */
    @Test
    public void doOnSubscribe_doOnNext_doOnCompleted__doOnUnsubscribe() {
        Subscription subscribe = Observable.just(1, 2, 3)
                .doOnCompleted(() -> mList.add("doOnCompleted"))
                .doOnNext(mList::add)
                .doOnSubscribe(() -> mList.add("doOnSubscribe"))
                .doOnUnsubscribe(() -> mList.add("doOnUnsubscribe"))
                .subscribe();

        subscribe.unsubscribe();

        assertEquals(mList, Arrays.asList("doOnSubscribe", 1, 2, 3, "doOnCompleted", "doOnUnsubscribe"));

    }

    //TODO-doOnRequest-参考http://blog.chengyunfeng.com/?p=981
    @Test
    public void doOnRequest() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                    }
                });
    }

    /**
     * register an action to take upon a variety of Observable lifecycle events
     *
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators
     * documentation: Do</a>
     */
    @Test
    public void doOnEach_doOnError() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(5 / 0);
            }
        })
                .doOnEach(notification -> {
                    String actionName = notification.getKind().name();
                    Object value = notification.getValue();
                    System.out.println("doOnEach--" + actionName + "->" + value);
                })
                .doOnError(throwable -> System.out.println("doOnError->" + throwable.getMessage()))
                .subscribe(
                        num -> {
                            System.out.println("subscribe--" + num);
                        },
                        throwable -> {
                            System.out.println("subscribe--" + throwable.getMessage());
                        }
                );
    }

    /**
     * registers an Action which will be called just before the resulting Observable terminates,
     * whether normally or with an error.
     *
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators
     * documentation: Do</a>
     */
    @Test
    public void doOnTerminate() {
        Observable.just(1)
                .doOnTerminate(() -> mList.add("doOnTerminate by Completed"))
                .subscribe();

        Observable.create(subscriber -> {
            subscriber.onError(new Exception("null"));
        })
                .doOnTerminate(() -> mList.add("doOnTerminate by Error"))
                .subscribe(
                        num -> {
                        },
                        throwable -> {
                        });

        assertEquals(mList, Arrays.asList("doOnTerminate by Completed", "doOnTerminate by Error"));
    }

    /**
     * The doAfterTerminate operator registers an Action which will be called just after the
     * resulting Observable terminates, whether normally or with an error.
     *
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators
     * documentation: Do</a>
     */
    @Test
    public void doAfterTerminate() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .doAfterTerminate(() -> mList.add("doAfterTerminate"))
                .subscribe(mList::add);

        assertEquals(mList, Arrays.asList(1, 2, 3, 4, 5, 6, "doAfterTerminate"));
    }

    /**
     * represent both the items emitted and the notifications sent as emitted items, or reverse
     * this process
     *
     * @see <a href="http://reactivex.io/documentation/operators/materialize-dematerialize.html">ReactiveX
     * operators documentation: Materialize</a>
     */
    @Test
    public void materialize() {
        Observable.just(1, 2)
                .materialize()
                .doOnNext(System.out::println)
                .subscribe(notification -> {
                    mList.add(notification.getKind().name() + "->" + notification.getValue());
                });
        assertEquals(mList, Arrays.asList("OnNext->1", "OnNext->2", "OnCompleted->null"));
    }

    /**
     * represent both the items emitted and the notifications sent as emitted items, or reverse
     * this process
     *
     * @see <a href="http://reactivex.io/documentation/operators/materialize-dematerialize.html">ReactiveX
     * operators documentation: Materialize</a>
     */
    @Test
    public void dematerialize() {
        Observable.just(1, 2)
                .materialize()
                .doOnNext(System.out::println)
                .dematerialize()
                .doOnNext(System.out::println)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 2));
    }

    /**
     * specify the scheduler on which an observer will observe this Observable
     *
     * @see <a href="http://reactivex.io/documentation/operators/observeon.html">ReactiveX operators
     * documentation: ObserveOn</a>
     */
    @Test
    public void observeOn() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("Observable's Thread = " + Thread.currentThread().getName());
                subscriber.onNext(1);
                subscriber.onCompleted();
            }
        })
                .observeOn(Schedulers.newThread())
                .subscribe((num) -> {
                    System.out.println("Subscriber's Thread = " + Thread.currentThread().getName());
                });
    }

    boolean isCompeleted = true;

    /**
     * TODO-serialize
     * force an Observable to make serialized calls and to be well-behaved
     *
     * @see <a href="http://reactivex.io/documentation/operators/serialize.html">ReactiveX operators
     * documentation: Serialize</a>
     */
    @Test
    public void serialize() {

        Observable<Integer> integerObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(2);
                if (isCompeleted) {
                    System.out.println(12345);
                    subscriber.onCompleted();
                } else {
                    subscriber.onNext(3);
                }
                subscriber.onCompleted();

            }
        });

        integerObservable
                .subscribeOn(Schedulers.newThread())
                .serialize()
                .subscribe(System.out::println);

        Utils.sleep(1000);
        isCompeleted = false;
        integerObservable.serialize().subscribe(System.out::println);
    }

    /**
     * operate upon the emissions and notifications from an Observable
     *
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators
     * documentation: Subscribe</a>
     */
    @Test
    public void subscribe() {
        Observable.just(1, 2, 3)
                .subscribe(mList::add,
                        (throwable) -> mList.add("Error"),
                        () -> mList.add("Complete"));
        assertEquals(mList, Arrays.asList(1, 2, 3, "Complete"));
    }

    /**
     * specify the scheduler an Observable should use when it is subscribed to
     *
     * @see <a href="http://reactivex.io/documentation/operators/subscribeon.html">ReactiveX
     * operators documentation: SubscribeOn</a>
     */
    @Test
    public void subscribeOn() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("Observable's Thread = " + Thread.currentThread().getName());
                subscriber.onNext(1);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .subscribe((num) -> {
                    System.out.println("Subscriber's Thread = " + Thread.currentThread().getName());
                });
    }

    /**
     * 根据官方宝蓝图实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/schedulers.png">schedulers.png</a>
     */
    @Test
    public void subscribeOn_and_observeOn() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                System.out.println("Observable's Thread = " + Thread.currentThread().getName());
                subscriber.onNext(1);
                subscriber.onCompleted();
            }
        })
                .observeOn(Schedulers.io())//决定了map()的线程
                .map(num -> {
                    System.out.println("map's Thread = " + Thread.currentThread().getName());
                    return num;
                })
                .subscribeOn(Schedulers.newThread())//决定了消息源的线程
                .observeOn(Schedulers.computation())//决定了订阅者的线程
                .subscribe((num) -> {
                    System.out.println("Subscriber's Thread = " + Thread.currentThread().getName());
                });

        //保证所有线程正常执行完毕
        Utils.sleep(1000);
    }

    /**
     * convert an Observable that emits items into one that emits indications of the amount of
     * time elapsed between those emissions
     *
     * @see <a href="http://reactivex.io/documentation/operators/timeinterval.html">ReactiveX
     * operators documentation: TimeInterval</a>
     */
    @Test
    public void timeInterval() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                Utils.sleep(500);
                subscriber.onNext(1);
                Utils.sleep(1000);
                subscriber.onNext(2);
                Utils.sleep(2000);
                subscriber.onNext(3);
                Utils.sleep(3000);
                subscriber.onCompleted();
            }
        })
                .take(5)
                .timeInterval()
                .subscribe(System.out::println);
    }

    /**
     * mirror the source Observable, but issue an error notification if a particular period of
     * time elapses without any emitted items
     *
     * @see <a href="http://reactivex.io/documentation/operators/timeout.html">ReactiveX operators
     * documentation: Timeout</a>
     */
    @Test
    public void timeout() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onNext(2);
                subscriber.onNext(3);
                subscriber.onNext(4);
                Utils.sleep(3000);
                subscriber.onNext(5);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .timeout(2, TimeUnit.SECONDS)
                .doOnError(System.out::println)
                .subscribe(num -> {
                    mList.add(num);
                }, throwable -> {
                    mList.add("throwable");
                });

        mTestScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList(1, 2, 3, 4, "throwable"));
    }

    /**
     * attach a timestamp to each item emitted by an Observable indicating when it was emitted
     *
     * @see <a href="http://reactivex.io/documentation/operators/timestamp.html">ReactiveX operators
     * documentation: Timestamp</a>
     */
    @Test
    public void timestamp() {
        System.out.println("start=" + System.currentTimeMillis());
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(1000);
                subscriber.onNext(2);
                Utils.sleep(3000);
                subscriber.onNext(3);
                subscriber.onCompleted();
            }
        })
                .timestamp()
                .subscribe(System.out::println);
        System.out.println("end" + System.currentTimeMillis());
    }

    /**
     * TODO-USING
     * create a disposable resource that has the same lifespan as the Observable
     *
     * @see <a href="http://reactivex.io/documentation/operators/using.html">ReactiveX operators
     * documentation: Using</a>
     */
    @Test
    public void using() {
        Observable.using(new Func0<Object>() {
            @Override
            public Object call() {
                return null;
            }
        }, new Func1<Object, Observable<?>>() {
            @Override
            public Observable<?> call(Object o) {
                return null;
            }
        }, new Action1<Object>() {
            @Override
            public void call(Object o) {

            }
        }).subscribe(System.out::println);
    }
}
