# RxJavaOperatorsUTSample
使用 UT 高效地玩转 RxJava 的操作符

##Features
- 使用纯 UT 实现 JxJava 的所有操作符，无需依赖 Android ，也不涉及太多测试技巧，专注于操作符的输入输出和处理
- 有目的性的输入与输出
  * 尽可能使用官方操作符的宝蓝图实现精确的输入和输出，如 `connect` 、 `replay` 、 `flatMap` 、 `concatMap` 等
  * 部分操作符使用[RxMarbles](http://rxmarbles.com/)进行实现，如 `combineLatest` 、 `amb` 等
- 一宝蓝图无法完全涵盖知识点的，配备了更多的UT和参考文章，如 `repeatWhen` 、 `retryWhen` 、 `defer` 等

##预备知识
- 测试线程和 RxJava 操作符所在线程如何顺利的执行完毕
- RxJava 提供的 `TestScheduler` 的用法
- 聚合操作符中的线程如何处理
- 预备知识的相关例子请查看 [ThreadTheory](https://github.com/geniusmart/RxJavaOperatorsUTSample/blob/master/app/src/test/java/com/geniusmart/rxjava/utils/ThreadTheory.java)

##Example
1. 区分 flatMap 和 concatMap 的宝蓝图
![flatMap](http://reactivex.io/documentation/operators/images/mergeMap.png)
![concatMap](http://reactivex.io/documentation/operators/images/concatMap.png)
 - 在这两张宝蓝图中，输入是完全一样的，但是输出结果不一致，concatMap 变换后保持原有的输入顺序，而flatMap则不然，使用 UT 分别来实现这两张宝蓝图：
 - flatMap的实现：
  ```java
    Observable.just(1, 2, 3)
            .flatMap((Func1<Integer, Observable<?>>) num -> Observable.interval(num - 1,
                    TimeUnit.SECONDS, mTestScheduler)
                    .take(2)
                    .map(value -> num + "◇"))
            .subscribe(mList::add);

    mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);
    assertEquals(mList, Arrays.asList("1◇", "1◇", "2◇", "3◇", "2◇", "3◇"));
    System.out.println(mList);
  ```

 - concatMap的实现：
  ```java
  Observable.just(1, 2, 3)
            .concatMap((Func1<Integer, Observable<?>>) num -> Observable.interval(num - 1,
                    TimeUnit.SECONDS, mTestScheduler)
                    .take(2)
                    .map(value -> num + "◇"))
            .subscribe(mList::add);

    mTestScheduler.advanceTimeBy(100, TimeUnit.SECONDS);
    assertEquals(mList, Arrays.asList("1◇", "1◇", "2◇", "2◇", "3◇", "3◇"));
    System.out.println(mList);
  ```
2. TODO

##所涵盖的操作符
- Creating Observables
 * crate、defer、empty/never/throw、from、interval、just、range、repeat、start、timer
- Transforming Observables
 * bufer、flatMap、concatMap、groupBy、map、scan、window
- Filtering Observables
 * debounce、distinct、elementAt、filter、first、ignoreElements、last、sample、skip、skipLast、take、takeLast
- Combining Observables
 * and/then/when、combineLatest、merge、startWith、switch、zip
- Error Handling Operators
 * catch、retry、retryWhen、onErrorReturn、onErrorResumeNext
- Observable Utility Operators
 * delay、do、materialize/dematerialize、observeOn、subscribe、timeInterval、timeout、timestamp
- Conditional and Boolean Operators
 * all、amb、contains、defaultIfEmpty、sequenceEqual、skipUntil、skipWhile、takeUntil、takeWhile
- Mathematical and Aggregate Operators
 * average、concat、count、max、min、reduce、sum

##TODO
- Backpressure Operators
- 自定义操作符
- join
- doOnRequest、serialize、using
- Subject
- 《使用 UT 高效地玩转 RxJava 的操作符》