package com.trigram.zero.flow;

import com.trigram.zero.flow.pair.DoublePair;
import com.trigram.zero.flow.pair.IntPair;
import com.trigram.zero.flow.pair.LongPair;
import com.trigram.zero.flow.pair.Pair;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;

/**
 * 对标{@link java.util.stream.Collector}接口，将{@link T} 转换成 {@link V}
 *
 * @param <T>
 *     原值，不必是数据，也可以是函数
 * @param <V>
 *     结果值，不必是数据，也可以是函数
 * @author wolray
 */
public interface Reducer<T, V> {

  /**
   * 平均数
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Double> average(ToDoubleFunction<T> function) {

    return average(function, null);
  }

  /**
   * 加权平均
   *
   * @param weightFunction
   *     为空时默认权重为1，就是求平均数
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Double> average(ToDoubleFunction<T> function, ToDoubleFunction<T> weightFunction) {

    BiConsumer<double[], T> biConsumer;
    if (weightFunction != null) {
      biConsumer = (a, t) -> {
        double v = function.applyAsDouble(t);
        double w = weightFunction.applyAsDouble(t);
        a[0] += v * w; // 数据*权重
        a[1] += w; // 权重
      };
    } else {
      biConsumer = (a, t) -> {
        a[0] += function.applyAsDouble(t);
        a[1] += 1; // 权重
      };
    }
    return Transducer.of(() -> new double[2], biConsumer, a -> a[1] != 0 ? a[0] / a[1] : 0);
  }

  /**
   * <p>collect.</p>
   *
   * @param des a {@link java.util.function.Supplier} object
   * @param <T> a T class
   * @param <C> a C class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, C extends Collection<T>> Reducer<T, C> collect(Supplier<C> des) {

    return of(des, Collection::add);
  }

  /**
   * <p>of.</p>
   *
   * @param supplier a {@link java.util.function.Supplier} object
   * @param accumulator a {@link java.util.function.BiConsumer} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V> Reducer<T, V> of(Supplier<V> supplier, BiConsumer<V, T> accumulator) {

    return of(supplier, accumulator, null);
  }

  /**
   * <p>of.</p>
   *
   * @param supplier a {@link java.util.function.Supplier} object
   * @param accumulator a {@link java.util.function.BiConsumer} object
   * @param finisher a {@link java.util.function.Consumer} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V> Reducer<T, V> of(Supplier<V> supplier, BiConsumer<V, T> accumulator, Consumer<V> finisher) {

    return new Reducer<T, V>() {

      @Override
      public BiConsumer<V, T> accumulator() {

        return accumulator;
      }

      @Override
      public Supplier<V> supplier() {

        return supplier;
      }

      @Override
      public Consumer<V> finisher() {

        return finisher;
      }
    };
  }

  /**
   * <p>count.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Integer> count() {

    return Transducer.of(() -> new int[1], (a, t) -> a[0]++, a -> a[0]);
  }

  /**
   * 不满足条件的数量
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Integer> countNot(Predicate<T> predicate) {

    return count(predicate.negate());
  }

  /**
   * 满足条件的数量
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Integer> count(Predicate<T> predicate) {

    return Transducer.of(() -> new int[1], (a, t) -> {
      if (predicate.test(t)) {
        a[0]++;
      }
    }, a -> a[0]);
  }

  /**
   * <p>filtering.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> filtering(Predicate<T> predicate) {

    return filtering(predicate, toList());
  }

  /**
   * <p>filtering.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V> Reducer<T, V> filtering(Predicate<T> predicate, Reducer<T, V> reducer) {

    BiConsumer<V, T> accumulator = reducer.accumulator();
    return of(reducer.supplier(), (v, t) -> {
      if (predicate.test(t)) {
        accumulator.accept(v, t);
      }
    }, reducer.finisher());
  }

  /**
   * <p>toList.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> toList() {

    return of(ArrayListZeroFlow::new, List::add);
  }

  /**
   * <p>filtering.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <T> a T class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T, V, E> Transducer<T, V, E> filtering(Predicate<T> predicate, Transducer<T, V, E> transducer) {

    return Transducer.of(filtering(predicate, transducer.reducer()), transducer.transformer());
  }

  /**
   * <p>join.</p>
   *
   * @param sep a {@link java.lang.String} object
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, String> join(String sep, Function<T, String> function) {

    return Transducer.of(() -> new StringJoiner(sep), (j, t) -> j.add(function.apply(t)), StringJoiner::toString);
  }

  /**
   * <p>mapping.</p>
   *
   * @param mapper a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, E> Reducer<T, ListZeroFlow<E>> mapping(Function<T, E> mapper) {

    return mapping(mapper, toList());
  }

  /**
   * <p>mapping.</p>
   *
   * @param mapper a {@link java.util.function.Function} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <T> a T class
   * @param <E> a E class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, E, V> Reducer<T, V> mapping(Function<T, E> mapper, Reducer<E, V> reducer) {

    BiConsumer<V, E> accumulator = reducer.accumulator();
    return of(reducer.supplier(), (v, t) -> {
      E e = mapper.apply(t);
      accumulator.accept(v, e);
    }, reducer.finisher());
  }

  /**
   * <p>mapping.</p>
   *
   * @param mapper a {@link java.util.function.Function} object
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <T> a T class
   * @param <R> a R class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T, R, V, E> Transducer<T, V, E> mapping(Function<T, R> mapper, Transducer<R, V, E> transducer) {

    return Transducer.of(mapping(mapper, transducer.reducer()), transducer.transformer());
  }

  /**
   * <p>max.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, T> max(Comparator<T> comparator) {

    return Transducer.of((t1, t2) -> comparator.compare(t1, t2) < 0 ? t2 : t1);
  }

  /**
   * <p>maxAtomicBy.</p>
   *
   * @param initValue a V object
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V extends Comparable<V>> Reducer<T, AtomicReference<Pair<T, V>>> maxAtomicBy(
      V initValue, Function<T, V> function
  ) {

    return of(() -> new AtomicReference<>(new Pair<>(null, initValue)), (ref, t) -> {
      V v = function.apply(t);
      ref.updateAndGet(p -> {
        if (p.second.compareTo(v) < 0) {
          p.set(t, v);
        }
        return p;
      });
    });
  }

  /**
   * <p>maxBy.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V extends Comparable<V>> Reducer<T, Pair<T, V>> maxBy(Function<T, V> function) {

    return of(() -> new Pair<>(null, null), (p, t) -> {
      V v = function.apply(t);
      if (p.second == null || p.second.compareTo(v) < 0) {
        p.set(t, v);
      }
    });
  }

  /**
   * <p>maxByInt.</p>
   *
   * @param function a {@link java.util.function.ToIntFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, IntPair<T>> maxByInt(ToIntFunction<T> function) {

    return of(() -> new IntPair<>(0, null), (p, t) -> {
      int v = function.applyAsInt(t);
      if (p.second == null || p.first < v) {
        p.first  = v;
        p.second = t;
      }
    });
  }

  /**
   * <p>maxByDouble.</p>
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, DoublePair<T>> maxByDouble(ToDoubleFunction<T> function) {

    return of(() -> new DoublePair<>(0, null), (p, t) -> {
      double v = function.applyAsDouble(t);
      if (p.second == null || p.first < v) {
        p.first  = v;
        p.second = t;
      }
    });
  }

  /**
   * <p>maxByLong.</p>
   *
   * @param function a {@link java.util.function.ToLongFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, LongPair<T>> maxByLong(ToLongFunction<T> function) {

    return of(() -> new LongPair<>(0, null), (p, t) -> {
      long v = function.applyAsLong(t);
      if (p.second == null || p.first < v) {
        p.first  = v;
        p.second = t;
      }
    });
  }

  /**
   * <p>min.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, T> min(Comparator<T> comparator) {

    return Transducer.of((t1, t2) -> comparator.compare(t1, t2) > 0 ? t2 : t1);
  }

  /**
   * <p>minAtomicBy.</p>
   *
   * @param initValue a V object
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V extends Comparable<V>> Reducer<T, AtomicReference<Pair<T, V>>> minAtomicBy(
      V initValue, Function<T, V> function
  ) {

    return of(() -> new AtomicReference<>(new Pair<>(null, initValue)), (ref, t) -> {
      V v = function.apply(t);
      ref.updateAndGet(p -> {
        if (p.second.compareTo(v) > 0) {
          p.set(t, v);
        }
        return p;
      });
    });
  }

  /**
   * <p>minBy.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V extends Comparable<V>> Reducer<T, Pair<T, V>> minBy(Function<T, V> function) {

    return of(() -> new Pair<>(null, null), (p, t) -> {
      V v = function.apply(t);
      if (p.second == null || p.second.compareTo(v) > 0) {
        p.set(t, v);
      }
    });
  }

  /**
   * <p>minByInt.</p>
   *
   * @param function a {@link java.util.function.ToIntFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, IntPair<T>> minByInt(ToIntFunction<T> function) {

    return of(() -> new IntPair<>(0, null), (p, t) -> {
      int v = function.applyAsInt(t);
      if (p.second == null || p.first > v) {
        p.first  = v;
        p.second = t;
      }
    });
  }

  /**
   * <p>minByDouble.</p>
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, DoublePair<T>> minByDouble(ToDoubleFunction<T> function) {

    return of(() -> new DoublePair<>(0, null), (p, t) -> {
      double v = function.applyAsDouble(t);
      if (p.second == null || p.first > v) {
        p.first  = v;
        p.second = t;
      }
    });
  }

  /**
   * <p>minByLong.</p>
   *
   * @param function a {@link java.util.function.ToLongFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, LongPair<T>> minByLong(ToLongFunction<T> function) {

    return of(() -> new LongPair<>(0, null), (p, t) -> {
      long v = function.applyAsLong(t);
      if (p.second == null || p.first > v) {
        p.first  = v;
        p.second = t;
      }
    });
  }

  /**
   * <p>partition.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, Pair<BatchedZeroFlow<T>, BatchedZeroFlow<T>>> partition(Predicate<T> predicate) {

    return partition(predicate, toBatched());
  }

  /**
   * <p>partition.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V> Reducer<T, Pair<V, V>> partition(Predicate<T> predicate, Reducer<T, V> reducer) {

    BiConsumer<V, T> accumulator = reducer.accumulator();
    Supplier<V>      supplier    = reducer.supplier();
    Consumer<V>      finisher    = reducer.finisher();
    return of(
        () -> new Pair<>(supplier.get(), supplier.get()),
        (p, t) -> accumulator.accept(predicate.test(t) ? p.first : p.second, t),
        finisher == null ? null : p -> {
          finisher.accept(p.first);
          finisher.accept(p.second);
        }
    );
  }

  /**
   * <p>toBatched.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, BatchedZeroFlow<T>> toBatched() {

    return of(BatchedZeroFlow::new, BatchedZeroFlow::add);
  }

  /**
   * <p>partition.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <T> a T class
   * @param <V> a V class
   * @param <R> a R class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T, V, R> Transducer<T, ?, Pair<R, R>> partition(Predicate<T> predicate, Transducer<T, V, R> transducer) {

    Function<V, R> mapper = transducer.transformer();
    return Transducer.of(
        partition(predicate, transducer.reducer()),
        p -> new Pair<>(mapper.apply(p.first), mapper.apply(p.second))
    );
  }

  /**
   * <p>reverse.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> reverse() {

    return Reducer.<T>toList().then(Collections::reverse);
  }

  /**
   * <p>sort.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> sort() {

    return sort((Comparator<T>) null);
  }

  /**
   * <p>sort.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> sort(Comparator<T> comparator) {

    return Reducer.<T>toList().then(ts -> ts.sort(comparator));
  }

  /**
   * <p>sort.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V extends Comparable<V>> Reducer<T, ListZeroFlow<T>> sort(Function<T, V> function) {

    return sort(Comparator.comparing(function));
  }

  /**
   * <p>sortDesc.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> sortDesc() {

    return sort(Collections.reverseOrder());
  }

  /**
   * <p>sortDesc.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> sortDesc(Comparator<T> comparator) {

    return sort(comparator.reversed());
  }

  /**
   * <p>sortDesc.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V extends Comparable<V>> Reducer<T, ListZeroFlow<T>> sortDesc(Function<T, V> function) {

    return sort(Comparator.comparing(function).reversed());
  }

  /**
   * <p>sum.</p>
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Double> sum(ToDoubleFunction<T> function) {

    return Transducer.of(() -> new double[1], (a, t) -> a[0] += function.applyAsDouble(t), a -> a[0]);
  }

  /**
   * <p>sumInt.</p>
   *
   * @param function a {@link java.util.function.ToIntFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Integer> sumInt(ToIntFunction<T> function) {

    return Transducer.of(() -> new int[1], (a, t) -> a[0] += function.applyAsInt(t), a -> a[0]);
  }

  /**
   * <p>sumLong.</p>
   *
   * @param function a {@link java.util.function.ToLongFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, Long> sumLong(ToLongFunction<T> function) {

    return Transducer.of(() -> new long[1], (a, t) -> a[0] += function.applyAsLong(t), a -> a[0]);
  }

  /**
   * <p>toConcurrentQueue.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ConcurrentQueueZeroFlow<T>> toConcurrentQueue() {

    return of(ConcurrentQueueZeroFlow::new, ConcurrentQueueZeroFlow::add);
  }

  /**
   * <p>toLinkedList.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, LinkedListZeroFlow<T>> toLinkedList() {

    return of(LinkedListZeroFlow::new, LinkedListZeroFlow::add);
  }

  /**
   * <p>toList.</p>
   *
   * @param initialCapacity a int
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, ListZeroFlow<T>> toList(int initialCapacity) {

    return of(() -> new ArrayListZeroFlow<>(initialCapacity), List::add);
  }

  /**
   * <p>toMap.</p>
   *
   * @param mapSupplier a {@link java.util.function.Supplier} object
   * @param toKey a {@link java.util.function.Function} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <T> a T class
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, K, V> Reducer<T, MapZeroFlow<K, V>> toMap(
      Supplier<? extends Map<K, V>> mapSupplier, Function<T, K> toKey, Reducer<T, V> reducer
  ) {

    Supplier<V>      supplier    = reducer.supplier();
    BiConsumer<V, T> accumulator = reducer.accumulator();
    Consumer<V>      finisher    = reducer.finisher();
    return of(
        () -> MapZeroFlow.of(mapSupplier.get()),
        (m, t) -> {
          // 对于将流中的原数据加入已有的map中的处理
          accumulator.accept(
              // 如果不存在这个key，就用supplier生成一个对应value
              m.computeIfAbsent(toKey.apply(t), k -> supplier.get()),
              t
          );
        },
        finisher == null ? null : m -> m.justValues().consume(finisher)
    );
  }

  /**
   * <p>toMap.</p>
   *
   * @param mapSupplier a {@link java.util.function.Supplier} object
   * @param toKey a {@link java.util.function.Function} object
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <T> a T class
   * @param <K> a K class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T, K, V, E> Transducer<T, ?, MapZeroFlow<K, E>> toMap(
      Supplier<? extends Map<K, V>> mapSupplier,
      Function<T, K> toKey, Transducer<T, V, E> transducer
  ) {

    return Transducer.of(
        toMap(mapSupplier, toKey, transducer.reducer()), m -> m.replaceValue(transducer.transformer()));
  }

  /**
   * <p>toMap.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param toValue a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, K, V> Reducer<T, MapZeroFlow<K, V>> toMap(Function<T, K> toKey, Function<T, V> toValue) {

    return toMap(MapZeroFlow::hash, toKey, toValue);
  }

  /**
   * <p>toMap.</p>
   *
   * @param mapSupplier a {@link java.util.function.Supplier} object
   * @param toKey a {@link java.util.function.Function} object
   * @param toValue a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, K, V> Reducer<T, MapZeroFlow<K, V>> toMap(
      Supplier<? extends Map<K, V>> mapSupplier, Function<T, K> toKey, Function<T, V> toValue
  ) {

    return of(() -> MapZeroFlow.of(mapSupplier.get()), (m, t) -> m.put(toKey.apply(t), toValue.apply(t)));
  }

  /**
   * <p>toMap.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <K> a K class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, K> Reducer<T, MapZeroFlow<K, T>> toMap(Function<T, K> toKey) {

    return toMap(() -> new LinkedHashMap<>(), toKey);
  }

  /**
   * <p>toMap.</p>
   *
   * @param mapSupplier a {@link java.util.function.Supplier} object
   * @param toKey a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <K> a K class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, K> Reducer<T, MapZeroFlow<K, T>> toMap(Supplier<? extends Map<K, T>> mapSupplier, Function<T, K> toKey) {

    return of(() -> MapZeroFlow.of(mapSupplier.get()), (m, t) -> m.put(toKey.apply(t), t));
  }

  /**
   * <p>toMapWithValue.</p>
   *
   * @param toValue a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V> Reducer<T, MapZeroFlow<T, V>> toMapWithValue(Function<T, V> toValue) {

    return toMapWithValue(LinkedHashMap::new, toValue);
  }

  /**
   * <p>toMapWithValue.</p>
   *
   * @param mapSupplier a {@link java.util.function.Supplier} object
   * @param toValue a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, V> Reducer<T, MapZeroFlow<T, V>> toMapWithValue(Supplier<Map<T, V>> mapSupplier, Function<T, V> toValue) {

    return of(() -> MapZeroFlow.of(mapSupplier.get()), (m, t) -> m.put(t, toValue.apply(t)));
  }

  /**
   * <p>toSet.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, SetZeroFlow<T>> toSet() {

    return of(LinkedHashSetZeroFlow::new, Set::add);
  }

  /**
   * <p>toSet.</p>
   *
   * @param initialCapacity a int
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T> Reducer<T, SetZeroFlow<T>> toSet(int initialCapacity) {

    return of(() -> new LinkedHashSetZeroFlow<>(initialCapacity), Set::add);
  }

  /**
   * <p>groupBy.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <T> a T class
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  static <T, K, V> Reducer<T, MapZeroFlow<K, V>> groupBy(Function<T, K> toKey, Reducer<T, V> reducer) {

    return toMap(HashMap::new, toKey, reducer);
  }

  /**
   * 等价{@link java.util.stream.Collector#accumulator()} ()}
   *
   * @see Collector#supplier()
   * @return a {@link java.util.function.BiConsumer} object
   */
  BiConsumer<V, T> accumulator();

  /**
   * 等价{@link java.util.stream.Collector#supplier()}
   *
   * @see Collector#supplier()
   * @return a {@link java.util.function.Supplier} object
   */
  Supplier<V> supplier();

  /**
   * 等价{@link java.util.stream.Collector#finisher()} ()}
   *
   * @see Collector#finisher() ()
   * @return a {@link java.util.function.Consumer} object
   */
  Consumer<V> finisher();

  /**
   * <p>then.</p>
   *
   * @param action a {@link java.util.function.Consumer} object
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  default Reducer<T, V> then(Consumer<V> action) {

    Consumer<V> finisher = finisher();
    return of(supplier(), accumulator(), finisher == null ? action : finisher.andThen(action));
  }

}
