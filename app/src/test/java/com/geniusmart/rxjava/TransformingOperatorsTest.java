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
import rx.functions.Func1;
import rx.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;

/**
 * Created by geniusmart on 2016/11/2.
 * <p>
 * Operators that transform items that are emitted by an Observable.
 */
public class TransformingOperatorsTest {

    private TestScheduler mTestScheduler;
    private List<Object> mList;

    @Before
    public void setUp() {
        mTestScheduler = new TestScheduler();
        mList = new ArrayList<>();
    }

    /**
     * periodically gather items emitted by an Observable into bundles and emit these bundles
     * rather than emitting the items one at a time
     *
     * @see <a href="http://reactivex.io/documentation/operators/buffer.html">ReactiveX
     * operators documentation: Buffer</a>
     */
    @Test
    public void buffer() {

        Observable.just(1, 2, 3, 4, 5, 6)
                .buffer(3)
                .subscribe(mList::add);

        System.out.println(mList);
        List<List<Integer>> exceptList = Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6));
        assertEquals(mList, exceptList);
    }

    /**
     * buffer(long,TimeUnit)
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/buffer5.png">
     * buffer diagram</a>
     */
    @Test
    public void bufferWithTimeUnit() {

        Observable.interval(0, 1, TimeUnit.SECONDS, mTestScheduler)
                .take(6)
                .buffer(2, TimeUnit.SECONDS, mTestScheduler)
                .subscribe(mList::add);


        mTestScheduler.advanceTimeBy(10, TimeUnit.SECONDS);
        System.out.println(mList);

        List<List<Long>> expectedList = Arrays.asList(Arrays.asList(0L, 1L), Arrays.asList(2L, 3L),
                Arrays.asList(4L, 5L));
        assertEquals(mList, expectedList);
    }

    //TODO
    @Test
    public void bufferClosingSelector() {

        Observable<Integer> observable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(1000);
                subscriber.onNext(2);
                subscriber.onNext(3);
                Utils.sleep(3);
                subscriber.onNext(4);
                subscriber.onNext(5);
                subscriber.onNext(6);
                subscriber.onCompleted();
            }
        })
                .doOnNext(System.out::println);


        Observable bufferOpenings = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Utils.sleep(500);
                subscriber.onNext("open");
                Utils.sleep(1000);
                subscriber.onNext("close");
                Utils.sleep(1000);
                subscriber.onNext("open");
                Utils.sleep(2000);
                subscriber.onNext("close");
                subscriber.onCompleted();

            }
        })
                .doOnNext(System.out::println);

        observable.buffer(bufferOpenings, new Func1() {
            @Override
            public Object call(Object o) {
                return o;
            }
        }).subscribe(System.out::println);
    }

    //TODO flatMap和concatmap可作为范例，实现宝蓝图的范例

    /**
     * transform the items emitted by an Observable into Observables, then flatten the
     * emissions from those into a single Observable
     * <p/>根据官方宝蓝图进行实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/flatmap.html">ReactiveX operators
     * documentation: FlatMap</a>
     * @see <a href="http://reactivex.io/documentation/operators/images/flatMap.c.png">faltMap.png</a>
     */
    @Test
    public void flatMap() {
        Observable.just(1, 2, 3)
                .flatMap((Func1<Integer, Observable<?>>) num -> Observable.interval(num - 1,
                        TimeUnit.SECONDS, mTestScheduler)
                        .take(2)
                        .map(value -> num + "◇"))
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList("1◇", "1◇", "2◇", "3◇", "2◇", "3◇"));
        System.out.println(mList);
    }

    /**
     * There is also a concatMap operator, which is like the simpler version of the flatMap
     * operator, but it concatenates rather than merges the resulting Observables in order to
     * generate its own sequence.
     * <p/>根据官方宝蓝图进行实现
     * <p/>
     * concatMap与flatMap的区别在于，concatMap可以保持Observable发送时的顺序，而flatMap可能会交错的
     * 发送数据流
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/concatMap.png">concatMap.png</a>
     */
    @Test
    public void concatMap() {
        Observable.just(1, 2, 3)
                .concatMap((Func1<Integer, Observable<?>>) num -> Observable.interval(num - 1,
                        TimeUnit.SECONDS, mTestScheduler)
                        .take(2)
                        .map(value -> num + "◇"))
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList("1◇", "1◇", "2◇", "2◇", "3◇", "3◇"));
        System.out.println(mList);
    }

    /**
     * It behaves much like flatMap, except that whenever a new item is emitted by the source
     * Observable, it will unsubscribe to and stop mirroring the Observable that was generated
     * from the previously-emitted item, and begin only mirroring the current one.
     * <p/>根据官方宝蓝图进行实现
     *
     * @see <a href="http://reactivex.io/documentation/operators/images/switchMap.png">switchMap.png</a>
     */
    @Test
    public void switchMap() {

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                Utils.sleep(1500);
                subscriber.onNext(2);
                Utils.sleep(500);
                subscriber.onNext(3);
                Utils.sleep(1500);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(mTestScheduler)
                .switchMap((Func1<Integer, Observable<?>>) num ->
                        Observable.interval(0, 1, TimeUnit.SECONDS)
                                .take(2)
                                .map(value -> num + "◇")
                )
                .subscribe(mList::add);

        mTestScheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertEquals(mList, Arrays.asList("1◇", "1◇", "2◇", "3◇", "3◇"));
        System.out.println(mList);
    }

    /**
     * divide an Observable into a set of Observables that each emit a different group of items
     * from the original Observable, organized by key
     *
     * @see <a href="http://reactivex.io/documentation/operators/groupby.html">ReactiveX operators
     * documentation: GroupBy</a>
     */
    @Test
    public void groupBy() {
        Observable.just(1, 2, 130, 3, 150, 999)
                .groupBy(num -> {

                    // 根据规则产生键
                    if (num > 100) {
                        return "big";
                    }
                    return "small";
                })
                .subscribe(groupedObservable -> {

                    groupedObservable.subscribe(value -> {
                        //通过getKey()可以获取键值
                        String key = groupedObservable.getKey();
                        String result = key + "->" + value;
                        System.out.println(result);
                        mList.add(result);
                    });
                });

        assertEquals(mList, Arrays.asList("small->1", "small->2", "big->130", "small->3",
                "big->150", "big->999"));

    }

    /**
     * transform the items emitted by an Observable by applying a function to each item
     *
     * @see <a href = "http://rxmarbles.com/#map">RxMarbles map diagrams</a>
     * @see <a href="http://reactivex.io/documentation/operators/map.html">ReactiveX operators
     * documentation: Map</a>
     */
    @Test
    public void map() {
        Observable.just(1, 2, 3)
                .map(integer -> integer * 10)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(10, 20, 30));
    }

    /**
     * apply a function to each item emitted by an Observable, sequentially, and emit each
     * successive value
     *
     * @see <a href = "http://rxmarbles.com/#scan">RxMarbles scan diagrams</a>
     * @see <a href="http://reactivex.io/documentation/operators/scan.html">ReactiveX operators
     * documentation: Scan</a>
     */
    @Test
    public void scan() {

        Observable.just(1, 2, 3, 4, 5)
                .scan((num1, num2) -> num1 + num2)
                .subscribe(mList::add);
        assertEquals(mList, Arrays.asList(1, 3, 6, 10, 15));

    }

    //TODO 与背压的关系？？

    /**
     * periodically subdivide items from an Observable into Observable windows and emit
     * these windows rather than emitting the items one at a time
     *
     * @see <a href="http://reactivex.io/documentation/operators/window.html">ReactiveX
     * operators documentation: Window</a>
     */
    @Test
    public void window() {

        List<Observable> list = new ArrayList<>();

        Observable.just(1, 2, 3, 4, 5, 6)
                .window(3)
                .toBlocking()
                .forEach(list::add);

        for (int i = 0; i < 2; i++) {
            Observable observable = list.get(i);
            System.out.println(i + "->" + observable);
            observable.subscribe(System.out::println);
        }

    }

}
