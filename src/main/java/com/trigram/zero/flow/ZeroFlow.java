package com.trigram.zero.flow;

import com.trigram.zero.flow.iterators.PickItr;
import com.trigram.zero.flow.pair.BooleanPair;
import com.trigram.zero.flow.pair.DoublePair;
import com.trigram.zero.flow.pair.IntPair;
import com.trigram.zero.flow.pair.LongPair;
import com.trigram.zero.flow.pair.Pair;
import com.trigram.zero.flow.pair.PairZeroFlow;
import com.trigram.zero.flow.triple.TripleConsumer;
import com.trigram.zero.flow.triple.TripleZeroFlow;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>ZeroFlow interface.</p>
 *
 * @author wolray
 */
public interface ZeroFlow<T> extends BaseZeroFlow<Consumer<T>> {

  //---------------------------核心方法----------------------------

  //---------------------------stream的快速构造操作----------------------------
  /**
   * <p>stop.</p>
   *
   * @param <T> a T class
   * @return a T object
   */
  static <T> T stop() {

    throw StopException.INSTANCE;
  }

  /**
   * 按以0为分界，给定的n一直自减，直到小于等于0后替换另一个处理逻辑。
   * <p>
   * 简单说就是多少个数据使用第三个参数消费，剩下的都是用第一个消费
   *
   * @param consumer
   *     小于等于0的处理
   * @param n
   *     n会一直自减
   * @param substitute
   *     大于0的处理
   */
  default void consume(Consumer<T> consumer, int n, Consumer<T> substitute) {

    if (n > 0) {
      int[] a = {n - 1};
      consume(t -> {
        if (a[0] < 0) {
          // 小于等于0的处理
          consumer.accept(t);
        } else {
          // 大于0的处理
          a[0]--;
          substitute.accept(t);
        }
      });
    } else {
      // 小于等于0的处理
      consume(consumer);
    }
  }

  /**
   * 附带索引下标的消费
   *
   * @param consumer a {@link com.trigram.zero.flow.ZeroFlow.IndexObjConsumer} object
   */
  default void consumeIndexed(IndexObjConsumer<T> consumer) {

    int[] a = {0};
    consume(t -> consumer.accept(a[0]++, t));
  }

  //---------------------------核心方法的重载----------------------------

  /**
   * 附带索引下标的可中断流的消费
   *
   * @param consumer a {@link com.trigram.zero.flow.ZeroFlow.IndexObjConsumer} object
   */
  default void consumeIndexedTillStop(IndexObjConsumer<T> consumer) {

    int[] a = {0};
    consumeTillStop(t -> consumer.accept(a[0]++, t));
  }

  /**
   * map/reduce理论中的核心map方法
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> map(Function<T, E> function) {

    return c -> consume(t -> c.accept(function.apply(t)));
  }

  /**
   * map/reduce理论中的核心reduce方法
   *
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <E> a E class
   * @return a E object
   */
  default <E> E reduce(Reducer<T, E> reducer) {

    E                des         = reducer.supplier().get();
    BiConsumer<E, T> accumulator = reducer.accumulator();
    consume(t -> accumulator.accept(des, t));
    Consumer<E> finisher = reducer.finisher();
    if (finisher != null) {
      finisher.accept(des);
    }
    return des;
  }

  /**
   * <p>map.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param n a int
   * @param substitute a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> map(Function<T, E> function, int n, Function<T, E> substitute) {

    return n <= 0 ? map(function) : c -> {
      int[] a = {n - 1};
      consume(t -> {
        if (a[0] < 0) {
          c.accept(function.apply(t));
        } else {
          a[0]--;
          c.accept(substitute.apply(t));
        }
      });
    };
  }

  /**
   * 附带索引下标的map处理
   *
   * @param function a {@link com.trigram.zero.flow.ZeroFlow.IndexObjFunction} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> mapIndexed(IndexObjFunction<T, E> function) {

    return c -> consumeIndexed((i, t) -> c.accept(function.apply(i, t)));
  }

  /**
   * 只处理不为null的数据
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> mapMaybe(Function<T, E> function) {

    return c -> consume(t -> {
      if (t != null) {
        c.accept(function.apply(t));
      }
    });
  }

  /**
   * 处理后不为空的数据才继续进入流中
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> mapNotNull(Function<T, E> function) {

    return c -> consume(t -> {
      E e = function.apply(t);
      if (e != null) {
        c.accept(e);
      }
    });
  }

  /**
   * 变成两个数据一组消费。比如： 1,2,3,4 -> [1,2],[3,4]，重叠交叉则变成：[1,2],[2,3],[3,4]
   *
   * @param overlapping
   *     每组数据是否重叠交叉
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default PairZeroFlow<T, T> mapPair(boolean overlapping) {

    return c -> reduce(new BooleanPair<>(false, (T) null), (p, t) -> {
      if (p.first) {
        c.accept(p.second, t);
      }
      p.first  = overlapping || !p.first;
      p.second = t;
    });
  }

  /**
   * 处理数据流中连续符合条件的部分，将连续符合条件部分收束成一个数据。类似处理字符串的子串
   *
   * @param takeWhile
   *     条件
   * @param reducer
   *     连续符合条件的数据收束器
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V> ZeroFlow<V> mapSub(Predicate<T> takeWhile, Reducer<T, V> reducer) {

    Supplier<V>      supplier    = reducer.supplier();
    BiConsumer<V, T> accumulator = reducer.accumulator();
    return c -> {
      V last = fold(null, (v, t) -> {
        if (takeWhile.test(t)) {
          if (v == null) {
            v = supplier.get();
          }
          accumulator.accept(v, t);
          return v;
        } else {
          if (v != null) {
            c.accept(v);
          }
          return null;
        }
      });
      if (last != null) {
        c.accept(last);
      }
    };
  }

  /**
   * <p>mapSub.</p>
   *
   * @see #mapSub(Predicate, Reducer)
   * @param takeWhile a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<ListZeroFlow<T>> mapSub(Predicate<T> takeWhile) {

    return mapSub(takeWhile, Reducer.toList());
  }

  /**
   * 类似{@link #mapSub(Predicate, Reducer)}。
   * <p>
   * 但是在每个子串收束后，会再收束一次， 然后用第二个谓词参数判断收束后是否符合条件，不符合则继续收束直到符合条件
   *
   * @param first a {@link java.util.function.Predicate} object
   * @param last a {@link java.util.function.Predicate} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V> ZeroFlow<V> mapSub(Predicate<T> first, Predicate<T> last, Reducer<T, V> reducer) {

    Supplier<V>      supplier    = reducer.supplier();
    BiConsumer<V, T> accumulator = reducer.accumulator();
    return c -> fold((V) null, (v, t) -> {
      if (v == null && first.test(t)) {
        v = supplier.get();
        accumulator.accept(v, t);
      } else if (v != null) {
        accumulator.accept(v, t);
        if (last.test(t)) {
          c.accept(v);
          return null;
        }
      }
      return v;
    });
  }

  /**
   * 条件默认用equals方法
   *
   * @see #mapSub(Predicate, Predicate, Reducer)
   * @param first a T object
   * @param last a T object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V> ZeroFlow<V> mapSub(T first, T last, Reducer<T, V> reducer) {

    return mapSub(first::equals, last::equals, reducer);
  }

  /**
   * <p>mapSub.</p>
   *
   * @see #mapSub(Object, Object, Reducer)
   * @param first a T object
   * @param last a T object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<ListZeroFlow<T>> mapSub(T first, T last) {

    return mapSub(first, last, Reducer.toList());
  }

  /**
   * 处理成int类型
   *
   * @return {@link com.trigram.zero.flow.IntZeroFlow}
   * @param function a {@link java.util.function.ToIntFunction} object
   */
  default IntZeroFlow mapToInt(ToIntFunction<T> function) {

    return c -> consume(t -> c.accept(function.applyAsInt(t)));
  }

  /**
   * map/reduce理论中的核心reduce方法
   *
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param transformer a {@link java.util.function.Function} object
   * @param <E> a E class
   * @param <V> a V class
   * @return a E object
   */
  default <E, V> E reduce(Reducer<T, V> reducer, Function<V, E> transformer) {

    return transformer.apply(reduce(reducer));
  }

  /**
   * map/reduce理论中的核心reduce方法
   *
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <E> a E class
   * @param <V> a V class
   * @return a E object
   */
  default <E, V> E reduce(Transducer<T, V, E> transducer) {

    return transducer.transformer().apply(reduce(transducer.reducer()));
  }

  /**
   * map/reduce理论中的核心reduce方法
   *
   * @param des
   *     原始值
   * @param accumulator
   *     对值的收束
   * @param <E> a E class
   * @return a E object
   */
  default <E> E reduce(E des, BiConsumer<E, T> accumulator) {

    consume(t -> accumulator.accept(des, t));
    return des;
  }

  /**
   * map/reduce理论中的核心reduce方法
   *
   * @param binaryOperator
   *     等价accumulator累加器
   * @return a T object
   */
  default T reduce(BinaryOperator<T> binaryOperator) {

    return reduce(Transducer.of(binaryOperator));
  }

  /**
   * <p>lazyReduce.</p>
   *
   * @param binaryOperator a {@link java.util.function.BinaryOperator} object
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default Lazy<T> lazyReduce(BinaryOperator<T> binaryOperator) {

    return Lazy.of(() -> reduce(binaryOperator));
  }

  /**
   * <p>unit.</p>
   *
   * @param t a T object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <T> ZeroFlow<T> unit(T t) {

    return c -> c.accept(t);
  }

  /**
   * <p>gen.</p>
   *
   * @param supplier a {@link java.util.function.Supplier} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ItrZeroFlow} object
   */
  static <T> ItrZeroFlow<T> gen(Supplier<T> supplier) {

    return () -> new Iterator<T>() {

      @Override
      public boolean hasNext() {

        return true;
      }

      @Override
      public T next() {

        return supplier.get();
      }
    };
  }

  /**
   * <p>gen.</p>
   *
   * @param seed a T object
   * @param operator a {@link java.util.function.UnaryOperator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <T> ZeroFlow<T> gen(T seed, UnaryOperator<T> operator) {

    return c -> {
      T t = seed;
      c.accept(t);
      while (true) {
        c.accept(t = operator.apply(t));
      }
    };
  }

  /**
   * <p>gen.</p>
   *
   * @param seed1 a T object
   * @param seed2 a T object
   * @param operator a {@link java.util.function.BinaryOperator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <T> ZeroFlow<T> gen(T seed1, T seed2, BinaryOperator<T> operator) {

    return c -> {
      T t1 = seed1, t2 = seed2;
      c.accept(t1);
      c.accept(t2);
      while (true) {
        c.accept(t2 = operator.apply(t1, t1 = t2));
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param map a {@link java.util.Map} object
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  static <K, V> MapZeroFlow<K, V> of(Map<K, V> map) {

    return MapZeroFlow.of(map);
  }

  /**
   * <p>of.</p>
   *
   * @param optional a {@link java.util.Optional} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <T> ZeroFlow<T> of(Optional<T> optional) {

    return optional::ifPresent;
  }

  /**
   * <p>of.</p>
   *
   * @param ts a T object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  @SafeVarargs
  static <T> ZeroFlow<T> of(T... ts) {

    return of(Arrays.asList(ts));
  }

  /**
   * <p>of.</p>
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <T> ZeroFlow<T> of(Iterable<T> iterable) {

    return iterable instanceof ItrZeroFlow ? (ItrZeroFlow<T>) iterable : (ItrZeroFlow<T>) iterable::iterator;
  }

  /**
   * <p>ofJson.</p>
   *
   * @param node a {@link java.lang.Object} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static ZeroFlow<Object> ofJson(Object node) {

    return ZeroFlow.ofTree(node, n -> c -> {
      if (n instanceof Iterable) {
        ((Iterable<?>) n).forEach(c);
      } else if (n instanceof Map) {
        ((Map<?, ?>) n).values().forEach(c);
      }
    });
  }

  /**
   * <p>ofTree.</p>
   *
   * @param node a N object
   * @param sub a {@link java.util.function.Function} object
   * @param <N> a N class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <N> ZeroFlow<N> ofTree(N node, Function<N, ZeroFlow<N>> sub) {

    return ExpandSeq.of(sub).toSeq(node);
  }

  /**
   * <p>ofTree.</p>
   *
   * @param maxDepth a int
   * @param node a N object
   * @param sub a {@link java.util.function.Function} object
   * @param <N> a N class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <N> ZeroFlow<N> ofTree(int maxDepth, N node, Function<N, ZeroFlow<N>> sub) {

    return ExpandSeq.of(sub).toSeq(node, maxDepth);
  }

  /**
   * <p>repeat.</p>
   *
   * @param n a int
   * @param t a T object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ItrZeroFlow} object
   */
  static <T> ItrZeroFlow<T> repeat(int n, T t) {

    return () -> new Iterator<T>() {

      int i = n;

      @Override
      public boolean hasNext() {

        return i > 0;
      }

      @Override
      public T next() {

        i--;
        return t;
      }
    };
  }

  /**
   * <p>flat.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  @SafeVarargs
  static <T> ZeroFlow<T> flat(ZeroFlow<T>... seq) {

    return c -> {
      for (ZeroFlow<T> s : seq) {
        s.consume(c);
      }
    };
  }

  /**
   * <p>flat.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <T> ZeroFlow<T> flat(ZeroFlow<Optional<T>> seq) {

    return c -> seq.consume(o -> o.ifPresent(c));
  }

  /**
   * <p>flatIterable.</p>
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ItrZeroFlow} object
   */
  static <T> ItrZeroFlow<T> flatIterable(Iterable<Optional<T>> iterable) {

    return () -> ItrUtil.flatOptional(iterable.iterator());
  }

  /**
   * <p>flatIterable.</p>
   *
   * @param iterables a {@link java.lang.Iterable} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ItrZeroFlow} object
   */
  @SafeVarargs
  static <T> ItrZeroFlow<T> flatIterable(Iterable<T>... iterables) {

    return () -> ItrUtil.flat(Arrays.asList(iterables).iterator());
  }

  /**
   * <p>flatIterable.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> flatIterable(Function<T, Iterable<E>> function) {

    return c -> consume(t -> function.apply(t).forEach(c));
  }

  /**
   * <p>flatMap.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> flatMap(Function<T, ZeroFlow<E>> function) {

    return c -> consume(t -> function.apply(t).consume(c));
  }

  /**
   * <p>flatOptional.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> flatOptional(Function<T, Optional<E>> function) {

    return c -> consume(t -> function.apply(t).ifPresent(c));
  }

  /**
   * <p>tillNull.</p>
   *
   * @param supplier a {@link java.util.function.Supplier} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ItrZeroFlow} object
   */
  static <T> ItrZeroFlow<T> tillNull(Supplier<T> supplier) {

    return () -> new PickItr<T>() {

      @Override
      public T pick() {

        T t = supplier.get();
        return t != null ? t : ZeroFlow.stop();
      }
    };
  }

  /**
   * <p>match.</p>
   *
   * @param s a {@link java.lang.String} object
   * @param pattern a {@link java.util.regex.Pattern} object
   * @return a {@link com.trigram.zero.flow.ItrZeroFlow} object
   */
  static ItrZeroFlow<Matcher> match(String s, Pattern pattern) {

    return () -> new Iterator<Matcher>() {

      final Matcher matcher = pattern.matcher(s);

      @Override
      public boolean hasNext() {

        return matcher.find();
      }

      @Override
      public Matcher next() {

        return matcher;
      }
    };
  }

  /**
   * <p>nothing.</p>
   *
   * @param <T> a T class
   * @return a {@link java.util.function.Consumer} object
   */
  @SuppressWarnings("unchecked")
  static <T> Consumer<T> nothing() {

    return (Consumer<T>) Empty.nothing;
  }

  /**
   * <p>empty.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  @SuppressWarnings("unchecked")
  static <T> ZeroFlow<T> empty() {

    return (ZeroFlow<T>) Empty.emptySeq;
  }

  //--------------------------stream的中间处理操作-----------------------------

  /**
   * <p>append.</p>
   *
   * @param t a T object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> append(T t) {

    return c -> {
      consume(c);
      c.accept(t);
    };
  }

  /**
   * <p>append.</p>
   *
   * @param t a T object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  @SuppressWarnings("unchecked")
  default ZeroFlow<T> append(T... t) {

    return c -> {
      consume(c);
      for (T x : t) {
        c.accept(x);
      }
    };
  }

  /**
   * <p>appendAll.</p>
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> appendAll(Iterable<T> iterable) {

    return c -> {
      consume(c);
      iterable.forEach(c);
    };
  }

  /**
   * <p>appendWith.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> appendWith(ZeroFlow<T> seq) {

    return c -> {
      consume(c);
      seq.consume(c);
    };
  }

  /**
   * <p>lazyReduce.</p>
   *
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default <E> Lazy<E> lazyReduce(Reducer<T, E> reducer) {

    return Lazy.of(() -> reduce(reducer));
  }

  /**
   * <p>lazyReduce.</p>
   *
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <E> a E class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default <E, V> Lazy<E> lazyReduce(Transducer<T, V, E> transducer) {

    return Lazy.of(() -> reduce(transducer));
  }

  /**
   * 查找指定条件的第一个数据
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link java.util.Optional} object
   */
  default Optional<T> find(Predicate<T> predicate) {

    Mutable<T> m = new Mutable<>(null);
    consumeTillStop(t -> {
      if (predicate.test(t)) {
        m.set(t);
        stop();
      }
    });
    return m.toOptional();
  }

  /**
   * <p>findNot.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link java.util.Optional} object
   */
  default Optional<T> findNot(Predicate<T> predicate) {

    return find(predicate.negate());
  }

  /**
   * 是否所有符合条件
   *
   * @return boolean
   * @param predicate a {@link java.util.function.Predicate} object
   */
  default boolean all(Predicate<T> predicate) {

    return !find(predicate.negate()).isPresent();
  }


  /**
   * <p>any.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a boolean
   */
  default boolean any(Predicate<T> predicate) {

    return find(predicate).isPresent();
  }

  /**
   * <p>anyNot.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a boolean
   */
  default boolean anyNot(Predicate<T> predicate) {

    return any(predicate.negate());
  }

  /**
   * 查找第一个重复的数据
   *
   * @return a {@link java.util.Optional} object
   */
  default Optional<T> findFirstDuplicate() {

    Set<T> set = new HashSet<>(sizeOrDefault());
    return find(t -> !set.add(t));
  }

  /**
   * <p>filter.</p>
   *
   * @param n a int
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filter(int n, Predicate<T> predicate) {

    return predicate == null ? this : c -> consume(c, n, t -> {
      if (predicate.test(t)) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>filter.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filter(Predicate<T> predicate) {

    return predicate == null ? this : c -> consume(t -> {
      if (predicate.test(t)) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>filterIn.</p>
   *
   * @param collection a {@link java.util.Collection} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filterIn(Collection<T> collection) {

    return collection == null ? this : filter(collection::contains);
  }

  /**
   * <p>filterIn.</p>
   *
   * @param map a {@link java.util.Map} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filterIn(Map<T, ?> map) {

    return map == null ? this : filter(map::containsKey);
  }

  /**
   * <p>filterNot.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filterNot(Predicate<T> predicate) {

    return predicate == null ? this : filter(predicate.negate());
  }

  /**
   * <p>filterNotIn.</p>
   *
   * @param collection a {@link java.util.Collection} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filterNotIn(Collection<T> collection) {

    return collection == null ? this : filterNot(collection::contains);
  }

  /**
   * <p>filterNotIn.</p>
   *
   * @param map a {@link java.util.Map} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filterNotIn(Map<T, ?> map) {

    return map == null ? this : filterNot(map::containsKey);
  }

  /**
   * <p>filterNotNull.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filterNotNull() {

    return filter(Objects::nonNull);
  }

  /**
   * <p>filterIndexed.</p>
   *
   * @param predicate a {@link com.trigram.zero.flow.ZeroFlow.IndexObjPredicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> filterIndexed(IndexObjPredicate<T> predicate) {

    return predicate == null ? this : c -> consumeIndexed((i, t) -> {
      if (predicate.test(i, t)) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>filterInstance.</p>
   *
   * @param cls a {@link java.lang.Class} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> filterInstance(Class<E> cls) {

    return c -> consume(t -> {
      if (cls.isInstance(t)) {
        c.accept(cls.cast(t));
      }
    });
  }

  /**
   * <p>asIterable.</p>
   *
   * @return a {@link com.trigram.zero.flow.ItrZeroFlow} object
   */
  default ItrZeroFlow<T> asIterable() {

    return toBatched();
  }

  /**
   * <p>toBatched.</p>
   *
   * @return a {@link com.trigram.zero.flow.BatchedZeroFlow} object
   */
  default BatchedZeroFlow<T> toBatched() {

    return reduce(new BatchedZeroFlow<>(), BatchedZeroFlow::add);
  }

  /**
   * 加权平均
   *
   * @return double
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @param weightFunction a {@link java.util.function.ToDoubleFunction} object
   */
  default double average(ToDoubleFunction<T> function, ToDoubleFunction<T> weightFunction) {

    return reduce(Reducer.average(function, weightFunction));
  }

  /**
   * <p>average.</p>
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @return a double
   */
  default double average(ToDoubleFunction<T> function) {

    return average(function, null);
  }

  /**
   * <p>chunked.</p>
   *
   * @param size a int
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V> ZeroFlow<V> chunked(int size, Reducer<T, V> reducer) {

    if (size <= 0) {
      throw new IllegalArgumentException("non-positive size");
    }
    Supplier<V>      supplier    = reducer.supplier();
    BiConsumer<V, T> accumulator = reducer.accumulator();
    Consumer<V>      finisher    = reducer.finisher();
    return c -> {
      IntPair<V> intPair = new IntPair<>(0, supplier.get());
      reduce(intPair, (p, t) -> {
        if (p.first == size) {
          if (finisher != null) {
            finisher.accept(p.second);
          }
          c.accept(p.second);
          p.second = supplier.get();
          p.first  = 0;
        }
        accumulator.accept(p.second, t);
        p.first++;
      });
      if (intPair.second != null) {
        c.accept(intPair.second);
      }
    };
  }

  /**
   * <p>chunked.</p>
   *
   * @param size a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<ListZeroFlow<T>> chunked(int size) {

    return chunked(size, Reducer.toList(size));
  }

  /**
   * <p>chunked.</p>
   *
   * @param size a int
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V, E> ZeroFlow<E> chunked(int size, Transducer<T, V, E> transducer) {

    return chunked(size, transducer.reducer()).map(transducer.transformer());
  }

  /**
   * 无限循环
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> circle() {

    return c -> {
      while (true) {
        consume(c);
      }
    };
  }

  /**
   * 收集到特定集合中
   *
   * @param constructor
   *     创建指定的集合的方法
   * @return {@link C}
   * @param <C> a C class
   */
  default <C extends Collection<T>> C collectBy(IntFunction<C> constructor) {

    return reduce(constructor.apply(sizeOrDefault()), Collection::add);
  }

  /**
   * 统计数量
   *
   * @return int
   */
  default int count() {

    return reduce(Reducer.count());
  }

  /**
   * 统计符合条件的数量
   *
   * @return int
   * @param predicate a {@link java.util.function.Predicate} object
   */
  default int count(Predicate<T> predicate) {

    return reduce(Reducer.count(predicate));
  }

  /**
   * 统计不符合条件的数量
   *
   * @return int
   * @param predicate a {@link java.util.function.Predicate} object
   */
  default int countNot(Predicate<T> predicate) {

    return reduce(Reducer.countNot(predicate));
  }

  /**
   * 去重
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> distinct() {

    return c -> reduce(new HashSet<>(), (set, t) -> {
      if (set.add(t)) {
        c.accept(t);
      }
    });
  }

  /**
   * 指定逻辑去重
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<T> distinctBy(Function<T, E> function) {

    return c -> reduce(new HashSet<>(), (set, t) -> {
      if (set.add(function.apply(t))) {
        c.accept(t);
      }
    });
  }

  /**
   * 前n个数据改处理，其余用原有处理
   *
   * @see #consume(Consumer, int, Consumer)
   * @param n a int
   * @param substitute a {@link java.util.function.Consumer} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> partial(int n, Consumer<T> substitute) {

    return c -> consume(c, n, substitute);
  }

  /**
   * 删除前n个
   *
   * @param n a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> drop(int n) {
    //等价于前面n个不做处理
    return n <= 0 ? this : partial(n, nothing());
  }

  /**
   * <p>dropWhile.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> dropWhile(Predicate<T> predicate) {

    return c -> foldBoolean(false, (b, t) -> {
      if (b || !predicate.test(t)) {
        c.accept(t);
        return true;
      }
      return false;
    });
  }

  /**
   * <p>foldBoolean.</p>
   *
   * @param init a boolean
   * @param function a {@link com.trigram.zero.flow.ZeroFlow.BooleanObjToBoolean} object
   * @return a boolean
   */
  default boolean foldBoolean(boolean init, BooleanObjToBoolean<T> function) {

    boolean[] a = {init};
    consume(t -> a[0] = function.apply(a[0], t));
    return a[0];
  }


  /**
   * <p>fold.</p>
   *
   * @return {@link E}
   * @see ItrZeroFlow#fold(Object, BiFunction)
   * @param init a E object
   * @param function a {@link java.util.function.BiFunction} object
   * @param <E> a E class
   */
  default <E> E fold(E init, BiFunction<E, T, E> function) {

    Mutable<E> m = new Mutable<>(init);
    consume(t -> m.it = function.apply(m.it, t));
    return m.it;
  }

  /**
   * <p>foldDouble.</p>
   *
   * @return double
   * @see ItrZeroFlow#fold(Object, BiFunction)
   * @param init a double
   * @param function a {@link com.trigram.zero.flow.ZeroFlow.DoubleObjToDouble} object
   */
  default double foldDouble(double init, DoubleObjToDouble<T> function) {

    double[] a = {init};
    consume(t -> a[0] = function.apply(a[0], t));
    return a[0];
  }

  /**
   * <p>foldLong.</p>
   *
   * @return long
   * @see ItrZeroFlow#fold(Object, BiFunction)
   * @param init a long
   * @param function a {@link com.trigram.zero.flow.ZeroFlow.LongObjToLong} object
   */
  default long foldLong(long init, LongObjToLong<T> function) {

    long[] a = {init};
    consume(t -> a[0] = function.apply(a[0], t));
    return a[0];
  }

  /**
   * 折叠任意类型
   *
   * @return {@link E}
   * @see ItrZeroFlow#fold(Object, BiFunction)
   * @param init a E object
   * @param function a {@link java.util.function.BiFunction} object
   * @param <E> a E class
   */
  default <E> E foldAtomic(E init, BiFunction<E, T, E> function) {

    AtomicReference<E> m = new AtomicReference<>(init);
    consume(t -> m.updateAndGet(e -> function.apply(e, t)));
    return m.get();
  }

  /**
   * 将流中数据折叠成一个结果
   *
   * @return int
   * @see ItrZeroFlow#fold(Object, BiFunction)
   * @param init a int
   * @param function a {@link com.trigram.zero.flow.ZeroFlow.IntObjToInt} object
   */
  default int foldInt(int init, IntObjToInt<T> function) {

    int[] a = {init};
    consume(t -> a[0] = function.apply(a[0], t));
    return a[0];
  }

  /**
   * <p>runningFold.</p>
   *
   * @return {@link com.trigram.zero.flow.IntZeroFlow}
   * @see ItrZeroFlow#runningFold(Object, BiFunction)
   * @param init a E object
   * @param function a {@link java.util.function.BiFunction} object
   * @param <E> a E class
   */
  default <E> ZeroFlow<E> runningFold(E init, BiFunction<E, T, E> function) {

    return c -> fold(init, (e, t) -> {
      e = function.apply(e, t);
      c.accept(e);
      return e;
    });
  }

  /**
   * 每个数据重复多少次在此之前所有的处理
   *
   * @param times a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> duplicateAll(int times) {

    return c -> {
      for (int i = 0; i < times; i++) {
        consume(c);
      }
    };
  }

  /**
   * 每个数据复制多少次继续传递给后续处理
   *
   * @param times a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> duplicateEach(int times) {

    return c -> consume(t -> {
      for (int i = 0; i < times; i++) {
        c.accept(t);
      }
    });
  }

  /**
   * 只有条件的数据才复制
   *
   * @see #duplicateEach(int)
   * @param times a int
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> duplicateIf(int times, Predicate<T> predicate) {

    return c -> consume(t -> {
      if (predicate.test(t)) {
        for (int i = 0; i < times; i++) {
          c.accept(t);
        }
      } else {
        c.accept(t);
      }
    });
  }

  /**
   * <p>groupBy.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param <K> a K class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K> MapZeroFlow<K, ListZeroFlow<T>> groupBy(Function<T, K> toKey) {

    return groupBy(toKey, Reducer.toList());
  }

  /**
   * <p>groupBy.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, V> MapZeroFlow<K, V> groupBy(Function<T, K> toKey, Reducer<T, V> reducer) {

    return reduce(Reducer.toMap(HashMap::new, toKey, reducer));
  }

  /**
   * <p>groupBy.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param operator a {@link java.util.function.BinaryOperator} object
   * @param <K> a K class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K> MapZeroFlow<K, T> groupBy(Function<T, K> toKey, BinaryOperator<T> operator) {

    return groupBy(toKey, Transducer.of(operator));
  }

  /**
   * <p>groupBy.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <K> a K class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, V, E> MapZeroFlow<K, E> groupBy(Function<T, K> toKey, Transducer<T, V, E> transducer) {

    return reduce(Reducer.toMap(HashMap::new, toKey, transducer));
  }

  /**
   * <p>groupBy.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param toValue a {@link java.util.function.Function} object
   * @param <K> a K class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, E> MapZeroFlow<K, ListZeroFlow<E>> groupBy(Function<T, K> toKey, Function<T, E> toValue) {

    return groupBy(toKey, Reducer.mapping(toValue));
  }

  /**
   * <p>first.</p>
   *
   * @return a T object
   */
  default T first() {

    Mutable<T> m = new Mutable<>(null);
    consumeTillStop(t -> {
      m.it = t;
      stop();
    });
    return m.it;
  }

  /**
   * <p>firstMaybe.</p>
   *
   * @return a {@link java.util.Optional} object
   */
  default Optional<T> firstMaybe() {

    return find(t -> true);
  }

  /**
   * <p>last.</p>
   *
   * @return a T object
   */
  default T last() {

    return reduce(new Mutable<T>(null), Mutable::set).it;
  }

  /**
   * <p>lastMaybe.</p>
   *
   * @return a {@link java.util.Optional} object
   */
  default Optional<T> lastMaybe() {

    Mutable<T> m = new Mutable<>(null);
    consume(m::set);
    return m.toOptional();
  }

  /**
   * <p>last.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link java.util.Optional} object
   */
  default Optional<T> last(Predicate<T> predicate) {

    return filter(predicate).lastMaybe();
  }

  /**
   * <p>lastNot.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link java.util.Optional} object
   */
  default Optional<T> lastNot(Predicate<T> predicate) {

    return last(predicate.negate());
  }

  /**
   * <p>lazyLast.</p>
   *
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default Lazy<T> lazyLast() {

    return new Mutable<T>(null) {

      @Override
      protected void eval() {

        consume(t -> it = t);
      }
    };
  }

  /**
   * <p>max.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a T object
   */
  default T max(Comparator<T> comparator) {

    return reduce(Reducer.max(comparator));
  }

  /**
   * <p>maxBy.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.pair.Pair} object
   */
  default <V extends Comparable<V>> Pair<T, V> maxBy(Function<T, V> function) {

    return reduce(Reducer.maxBy(function));
  }

  /**
   * <p>min.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a T object
   */
  default T min(Comparator<T> comparator) {

    return reduce(Reducer.min(comparator));
  }

  /**
   * <p>minBy.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.pair.Pair} object
   */
  default <V extends Comparable<V>> Pair<T, V> minBy(Function<T, V> function) {

    return reduce(Reducer.minBy(function));
  }

  /**
   * 有一个符合就是返回false
   *
   * @return boolean
   * @param predicate a {@link java.util.function.Predicate} object
   */
  default boolean none(Predicate<T> predicate) {

    return !find(predicate).isPresent();
  }

  /**
   * <p>onEach.</p>
   *
   * @param consumer a {@link java.util.function.Consumer} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> onEach(Consumer<T> consumer) {

    return c -> consume(consumer.andThen(c));
  }

  /**
   * <p>onEach.</p>
   *
   * @param n a int
   * @param consumer a {@link java.util.function.Consumer} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> onEach(int n, Consumer<T> consumer) {

    return c -> consume(c, n, consumer.andThen(c));
  }

  /**
   * <p>onEachIndexed.</p>
   *
   * @param consumer a {@link com.trigram.zero.flow.ZeroFlow.IndexObjConsumer} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> onEachIndexed(IndexObjConsumer<T> consumer) {

    return c -> consumeIndexed((i, t) -> {
      consumer.accept(i, t);
      c.accept(t);
    });
  }

  /**
   * <p>pair.</p>
   *
   * @param f1 a {@link java.util.function.Function} object
   * @param f2 a {@link java.util.function.Function} object
   * @param <A> a A class
   * @param <B> a B class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <A, B> PairZeroFlow<A, B> pair(Function<T, A> f1, Function<T, B> f2) {

    return c -> consume(t -> c.accept(f1.apply(t), f2.apply(t)));
  }

  /**
   * <p>pairBy.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <E> PairZeroFlow<E, T> pairBy(Function<T, E> function) {

    return c -> consume(t -> c.accept(function.apply(t), t));
  }

  /**
   * <p>pairByNotNull.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <E> PairZeroFlow<E, T> pairByNotNull(Function<T, E> function) {

    return c -> consume(t -> {
      E e = function.apply(t);
      if (e != null) {
        c.accept(e, t);
      }
    });
  }

  /**
   * <p>pairWith.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <E> PairZeroFlow<T, E> pairWith(Function<T, E> function) {

    return c -> consume(t -> c.accept(t, function.apply(t)));
  }

  /**
   * <p>pairWithNotNull.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <E> PairZeroFlow<T, E> pairWithNotNull(Function<T, E> function) {

    return c -> consume(t -> {
      E e = function.apply(t);
      if (e != null) {
        c.accept(t, e);
      }
    });
  }

  /**
   * <p>parallel.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> parallel() {

    return parallel(Async.common());
  }

  /**
   * <p>parallel.</p>
   *
   * @param async a {@link com.trigram.zero.flow.Async} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> parallel(Async async) {

    return c -> async.joinAll(map(t -> () -> c.accept(t)));
  }

  /**
   * <p>parallelNoJoin.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> parallelNoJoin() {

    return parallelNoJoin(Async.common());
  }

  /**
   * <p>parallelNoJoin.</p>
   *
   * @param async a {@link com.trigram.zero.flow.Async} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> parallelNoJoin(Async async) {

    return c -> consume(t -> async.submit(() -> c.accept(t)));
  }

  /**
   * <p>println.</p>
   */
  default void println() {

    consume(System.out::println);
  }

  /**
   * <p>printAll.</p>
   *
   * @param sep a {@link java.lang.String} object
   */
  default void printAll(String sep) {

    if ("\n".equals(sep)) {
      println();
    } else {
      System.out.println(join(sep));
    }
  }

  /**
   * <p>join.</p>
   *
   * @param sep a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   */
  default String join(String sep) {

    return join(sep, Object::toString);
  }

  /**
   * <p>join.</p>
   *
   * @param sep a {@link java.lang.String} object
   * @param function a {@link java.util.function.Function} object
   * @return a {@link java.lang.String} object
   */
  default String join(String sep, Function<T, String> function) {

    return reduce(new StringJoiner(sep), (j, t) -> j.add(function.apply(t))).toString();
  }

  /**
   * <p>replace.</p>
   *
   * @param n a int
   * @param operator a {@link java.util.function.UnaryOperator} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> replace(int n, UnaryOperator<T> operator) {

    return c -> consume(c, n, t -> c.accept(operator.apply(t)));
  }

  /**
   * <p>reverse.</p>
   *
   * @return a {@link com.trigram.zero.flow.ListZeroFlow} object
   */
  default ListZeroFlow<T> reverse() {

    return reduce(Reducer.reverse());
  }

  /**
   * <p>sortWith.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> sortWith(Comparator<T> comparator) {

    return c -> {
      ListZeroFlow<T> list = toList();
      list.sort(comparator);
      list.consume(c);
    };
  }

  /**
   * <p>sortBy.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E extends Comparable<E>> ZeroFlow<T> sortBy(Function<T, E> function) {

    return sortWith(Comparator.comparing(function));
  }

  /**
   * <p>sortCached.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E extends Comparable<E>> ZeroFlow<T> sortCached(Function<T, E> function) {

    return map(t -> new Pair<>(t, function.apply(t))).sortBy(p -> p.second).map(p -> p.first);
  }

  /**
   * <p>sortByDesc.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E extends Comparable<E>> ZeroFlow<T> sortByDesc(Function<T, E> function) {

    return sortWith(Comparator.comparing(function).reversed());
  }

  /**
   * <p>sortCachedDesc.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E extends Comparable<E>> ZeroFlow<T> sortCachedDesc(Function<T, E> function) {

    return map(t -> new Pair<>(t, function.apply(t))).sortByDesc(p -> p.second).map(p -> p.first);
  }

  /**
   * <p>sorted.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> sorted() {

    return sortWith(null);
  }

  /**
   * <p>sortedDesc.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> sortedDesc() {

    return sortWith(Collections.reverseOrder());
  }

  /**
   * <p>sortWithDesc.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> sortWithDesc(Comparator<T> comparator) {

    return sortWith(comparator.reversed());
  }

  /**
   * <p>sum.</p>
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @return a double
   */
  default double sum(ToDoubleFunction<T> function) {

    return reduce(Reducer.sum(function));
  }

  /**
   * <p>sumInt.</p>
   *
   * @param function a {@link java.util.function.ToIntFunction} object
   * @return a int
   */
  default int sumInt(ToIntFunction<T> function) {

    return reduce(Reducer.sumInt(function));
  }

  /**
   * <p>sumLong.</p>
   *
   * @param function a {@link java.util.function.ToLongFunction} object
   * @return a long
   */
  default long sumLong(ToLongFunction<T> function) {

    return reduce(Reducer.sumLong(function));
  }

  /**
   * 获取前n个数据
   *
   * @param n a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> take(int n) {

    return n <= 0 ? empty() : c -> {
      int[] i = {n};
      consumeTillStop(t -> {
        c.accept(t);
        if (--i[0] == 0) {
          stop();
        }
      });
    };
  }

  /**
   * 当通过转换后还符合条件就获取，否则立马终止
   *
   * @param function a {@link java.util.function.Function} object
   * @param testPrevCurr a {@link java.util.function.BiPredicate} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<T> takeWhile(Function<T, E> function, BiPredicate<E, E> testPrevCurr) {

    return c -> {
      Mutable<E> m = new Mutable<>(null);
      consumeTillStop(t -> {
        E curr = function.apply(t);
        if (m.it == null || testPrevCurr.test(m.it, curr)) {
          c.accept(t);
          m.it = curr;
        } else {
          stop();
        }
      });
    };
  }

  /**
   * <p>takeWhile.</p>
   *
   * @param testPrevCurr a {@link java.util.function.BiPredicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> takeWhile(BiPredicate<T, T> testPrevCurr) {

    return takeWhile(t -> t, testPrevCurr);
  }

  /**
   * 符合条件就获取，一旦不符合就停止
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> takeWhile(Predicate<T> predicate) {

    return c -> consumeTillStop(t -> {
      if (predicate.test(t)) {
        c.accept(t);
      } else {
        stop();
      }
    });
  }

  /**
   * 默认获取前面相同的所有数据，直到出现不相同数据为止
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> takeWhileEquals() {

    return takeWhile(t -> t, Objects::equals);
  }

  /**
   * 只不过用转换处理后的数据判断
   *
   * @see #takeWhileEquals()
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<T> takeWhileEquals(Function<T, E> function) {

    return takeWhile(function, Objects::equals);
  }

  /**
   * 限时处理数据，超时停止
   *
   * @param millis a long
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> timeLimit(long millis) {

    return millis * 1000000 <= 0 ? this : c -> {
      long end = System.nanoTime() + millis;
      consumeTillStop(t -> {
        if (System.nanoTime() > end) {
          stop();
        }
        c.accept(t);
      });
    };
  }

  /**
   * <p>toObjArray.</p>
   *
   * @param initializer a {@link java.util.function.IntFunction} object
   * @return an array of T[] objects
   */
  default T[] toObjArray(IntFunction<T[]> initializer) {

    SizedZeroFlow<T> ts = cache();
    T[]              a  = initializer.apply(ts.size());
    ts.consumeIndexed((i, t) -> a[i] = t);
    return a;
  }

  /**
   * 缓存之前所有处理后的数据
   *
   * @return a {@link com.trigram.zero.flow.SizedZeroFlow} object
   */
  default SizedZeroFlow<T> cache() {

    return toBatched();
  }

  /**
   * <p>toList.</p>
   *
   * @return a {@link com.trigram.zero.flow.ListZeroFlow} object
   */
  default ListZeroFlow<T> toList() {

    return reduce(new ArrayListZeroFlow<>(sizeOrDefault()), ArrayListZeroFlow::add);
  }

  /**
   * <p>toIntArray.</p>
   *
   * @param function a {@link java.util.function.ToIntFunction} object
   * @return an array of {@link int} objects
   */
  default int[] toIntArray(ToIntFunction<T> function) {

    SizedZeroFlow<T> ts = cache();
    int[]            a  = new int[ts.size()];
    ts.consumeIndexed((i, t) -> a[i] = function.applyAsInt(t));
    return a;
  }

  /**
   * <p>toDoubleArray.</p>
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @return an array of {@link double} objects
   */
  default double[] toDoubleArray(ToDoubleFunction<T> function) {

    SizedZeroFlow<T> ts = cache();
    double[]         a  = new double[ts.size()];
    ts.consumeIndexed((i, t) -> a[i] = function.applyAsDouble(t));
    return a;
  }

  /**
   * <p>toLongArray.</p>
   *
   * @param function a {@link java.util.function.ToLongFunction} object
   * @return an array of {@link long} objects
   */
  default long[] toLongArray(ToLongFunction<T> function) {

    SizedZeroFlow<T> ts = cache();
    long[]           a  = new long[ts.size()];
    ts.consumeIndexed((i, t) -> a[i] = function.applyAsLong(t));
    return a;
  }

  /**
   * <p>toBooleanArray.</p>
   *
   * @param function a {@link java.util.function.Predicate} object
   * @return an array of {@link boolean} objects
   */
  default boolean[] toBooleanArray(Predicate<T> function) {

    SizedZeroFlow<T> ts = cache();
    boolean[]        a  = new boolean[ts.size()];
    ts.consumeIndexed((i, t) -> a[i] = function.test(t));
    return a;
  }

  /**
   * <p>toConcurrentQueue.</p>
   *
   * @return a {@link com.trigram.zero.flow.ConcurrentQueueZeroFlow} object
   */
  default ConcurrentQueueZeroFlow<T> toConcurrentQueue() {

    return reduce(new ConcurrentQueueZeroFlow<>(), ConcurrentQueueZeroFlow::add);
  }

  /**
   * <p>toLazy.</p>
   *
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default <E> Lazy<E> toLazy(Reducer<T, E> reducer) {

    return Lazy.of(() -> reduce(reducer));
  }

  /**
   * <p>toLazy.</p>
   *
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default <V, E> Lazy<E> toLazy(Transducer<T, V, E> transducer) {

    return Lazy.of(() -> reduce(transducer));
  }

  /**
   * <p>toLinkedList.</p>
   *
   * @return a {@link com.trigram.zero.flow.LinkedListZeroFlow} object
   */
  default LinkedListZeroFlow<T> toLinkedList() {

    return reduce(new LinkedListZeroFlow<>(), LinkedListZeroFlow::add);
  }

  /**
   * <p>toMap.</p>
   *
   * @param mapSupplier a {@link java.util.function.Supplier} object
   * @param toKey a {@link java.util.function.Function} object
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <K> a K class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, V, E> MapZeroFlow<K, E> toMap(
      Supplier<MapZeroFlow<K, V>> mapSupplier, Function<T, K> toKey, Transducer<T, V, E> transducer
  ) {

    return reduce(Reducer.toMap(mapSupplier, toKey, transducer));
  }

  /**
   * <p>toMap.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <K> a K class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, V, E> MapZeroFlow<K, E> toMap(Function<T, K> toKey, Transducer<T, V, E> transducer) {

    return toMap(MapZeroFlow::hash, toKey, transducer);
  }

  /**
   * <p>toMap.</p>
   *
   * @param mapSupplier a {@link java.util.function.Supplier} object
   * @param toKey a {@link java.util.function.Function} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, V> MapZeroFlow<K, V> toMap(
      Supplier<MapZeroFlow<K, V>> mapSupplier, Function<T, K> toKey, Reducer<T, V> reducer
  ) {

    return toMap(mapSupplier, toKey, Transducer.of(reducer, x -> x));
  }

  /**
   * <p>toMap.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, V> MapZeroFlow<K, V> toMap(Function<T, K> toKey, Reducer<T, V> reducer) {

    return toMap(MapZeroFlow::hash, toKey, reducer);
  }

  /**
   * <p>toMap.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param toValue a {@link java.util.function.Function} object
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K, V> MapZeroFlow<K, V> toMap(Function<T, K> toKey, Function<T, V> toValue) {

    return reduce(Reducer.toMap(() -> new LinkedHashMap<>(sizeOrDefault()), toKey, toValue));
  }

  /**
   * <p>toMap.</p>
   *
   * @param toKey a {@link java.util.function.Function} object
   * @param <K> a K class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <K> MapZeroFlow<K, T> toMap(Function<T, K> toKey) {

    return toMap(toKey, v -> v);
  }

  /**
   * <p>toMapWithValue.</p>
   *
   * @param toValue a {@link java.util.function.Function} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <V> MapZeroFlow<T, V> toMapWithValue(Function<T, V> toValue) {

    return toMap(k -> k, toValue);
  }

  /**
   * <p>toSet.</p>
   *
   * @return a {@link com.trigram.zero.flow.SetZeroFlow} object
   */
  default SetZeroFlow<T> toSet() {

    return reduce(Reducer.toSet(sizeOrDefault()));
  }

  /**
   * <p>triple.</p>
   *
   * @param consumer a {@link java.util.function.BiConsumer} object
   * @param <A> a A class
   * @param <B> a B class
   * @param <D> a D class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <A, B, D> TripleZeroFlow<A, B, D> triple(BiConsumer<TripleConsumer<A, B, D>, T> consumer) {

    return c -> consume(t -> consumer.accept(c, t));
  }

  /**
   * <p>triple.</p>
   *
   * @param f1 a {@link java.util.function.Function} object
   * @param f2 a {@link java.util.function.Function} object
   * @param f3 a {@link java.util.function.Function} object
   * @param <A> a A class
   * @param <B> a B class
   * @param <D> a D class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <A, B, D> TripleZeroFlow<A, B, D> triple(Function<T, A> f1, Function<T, B> f2, Function<T, D> f3) {

    return c -> consume(t -> c.accept(f1.apply(t), f2.apply(t), f3.apply(t)));
  }

  /**
   * 通过滑动窗口生产数据，
   *
   * @param size
   *     每个窗口的大小，既是原数据流中多少个数据生产一个窗口包括的数据
   * @param step
   *     上一个窗口的第一个数据滑动多少个数据才形成下一个窗口的第一个数据
   * @param allowPartial
   *     剩余不足窗口数据量的窗口是否生产出来
   * @param reducer
   *     窗口的类型
   * @param <V>
   *     窗口的类型，既是容纳窗口中数据的容器类型
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V> ZeroFlow<V> windowed(int size, int step, boolean allowPartial, Reducer<T, V> reducer) {

    if (size <= 0 || step <= 0) {
      throw new IllegalArgumentException("non-positive size or step");
    }
    return c -> {
      Supplier<V>       supplier    = reducer.supplier();
      BiConsumer<V, T>  accumulator = reducer.accumulator();
      Consumer<V>       finisher    = reducer.finisher();
      Queue<IntPair<V>> queue       = new LinkedList<>();
      foldInt(0, (left, t) -> {
        // t是已有序列的数据
        if (left == 0) {
          // 当等于0的时候就创建一个窗口，然后将间隔step赋值，
          // 控制每次折叠数据的时候自减到下一个窗口的起始数据时再创建下一个窗口
          left = step;
          queue.offer(new IntPair<>(0, supplier.get()));
        }
        queue.forEach(sub -> {
          //将当前数据添加到所有此刻打开的窗口中
          accumulator.accept(sub.second, t);
          sub.first++;
        });
        IntPair<V> first = queue.peek();
        if (first != null && first.first == size) {
          // 当第一个窗口满的时候移除
          queue.poll();
          if (finisher != null) {
            finisher.accept(first.second);
          }
          // 并将窗口生产
          c.accept(first.second);
        }
        return left - 1;
      });
      if (allowPartial) {
        //剩余不足数量的窗口生产
        queue.forEach(p -> c.accept(p.second));
      }
      queue.clear();
    };
  }

  /**
   * <p>windowed.</p>
   *
   * @see #windowed(int, int, boolean, Reducer)
   * @param size a int
   * @param step a int
   * @param allowPartial a boolean
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<ListZeroFlow<T>> windowed(int size, int step, boolean allowPartial) {

    return windowed(size, step, allowPartial, Reducer.toList());
  }


  /**
   * <p>windowed.</p>
   *
   * @see #windowed(int, int, boolean, Reducer)
   * @param size a int
   * @param step a int
   * @param allowPartial a boolean
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V, E> ZeroFlow<E> windowed(int size, int step, boolean allowPartial, Transducer<T, V, E> transducer) {

    return windowed(size, step, allowPartial, transducer.reducer()).map(transducer.transformer());
  }

  /**
   * 按时间的滑动窗口实现
   *
   * @param timeMillis a long
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V> ZeroFlow<V> windowedByTime(long timeMillis, Reducer<T, V> reducer) {

    if (timeMillis <= 0) {
      throw new IllegalArgumentException("non-positive time");
    }
    return c -> {
      Supplier<V>      supplier    = reducer.supplier();
      BiConsumer<V, T> accumulator = reducer.accumulator();
      Consumer<V>      finisher    = reducer.finisher();
      reduce(new LongPair<>(System.currentTimeMillis(), supplier.get()), (p, t) -> {
        long now = System.currentTimeMillis();
        if (now - p.first > timeMillis) {
          // 超过给定的时间时隔就重置窗口开始时间
          p.first = now;
          if (finisher != null) {
            finisher.accept(p.second);
          }
          // 将上一个窗口生产
          c.accept(p.second);
          //重新创建一个窗口
          p.second = supplier.get();
        }
        accumulator.accept(p.second, t);
      });
    };
  }

  /**
   * 类似{@link #windowed(int, int, boolean, Reducer)}，只是把控制数量变成控制时间
   *
   * @param timeMillis
   *     一个窗口产生多少时间数据
   * @param stepMillis
   *     每个窗口间隔多少时间才创建
   * @param reducer
   *     生产窗口的类型
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V> ZeroFlow<V> windowedByTime(long timeMillis, long stepMillis, Reducer<T, V> reducer) {

    if (timeMillis <= 0 || stepMillis <= 0) {
      throw new IllegalArgumentException("non-positive time or step");
    }
    return c -> {
      Supplier<V>        supplier    = reducer.supplier();
      BiConsumer<V, T>   accumulator = reducer.accumulator();
      Consumer<V>        finisher    = reducer.finisher();
      Queue<LongPair<V>> queue       = new LinkedList<>();
      long[]             last        = {System.currentTimeMillis(), 0};
      reduce(last, (a, t) -> {
        if (a[1] <= 0) {
          a[1] = stepMillis;
          queue.offer(new LongPair<>(System.currentTimeMillis(), supplier.get()));
        }
        queue.forEach(sub -> accumulator.accept(sub.second, t));
        LongPair<V> first = queue.peek();
        if (first != null && System.currentTimeMillis() - first.first > timeMillis) {
          queue.poll();
          if (finisher != null) {
            finisher.accept(first.second);
          }
          c.accept(first.second);
        }
        long now = System.currentTimeMillis();
        a[1] -= now - a[0];
        a[0] = now;
      });
      queue.clear();
    };
  }

  /**
   * <p>windowedByTime.</p>
   *
   * @see #windowedByTime(long, Reducer)
   * @param timeMillis a long
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<ListZeroFlow<T>> windowedByTime(long timeMillis) {

    return windowedByTime(timeMillis, Reducer.toList());
  }

  /**
   * <p>windowedByTime.</p>
   *
   * @see #windowedByTime(long, long, Reducer)
   * @param timeMillis a long
   * @param stepMillis a long
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<ListZeroFlow<T>> windowedByTime(long timeMillis, long stepMillis) {

    return windowedByTime(timeMillis, stepMillis, Reducer.toList());
  }

  /**
   * <p>windowedByTime.</p>
   *
   * @see #windowedByTime(long, long, Reducer)
   * @param timeMillis a long
   * @param stepMillis a long
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <V, E> ZeroFlow<E> windowedByTime(long timeMillis, long stepMillis, Transducer<T, V, E> transducer) {

    return windowedByTime(timeMillis, stepMillis, transducer.reducer()).map(transducer.transformer());
  }

  /**
   * 按时间开窗
   *
   * @param timeMillis a long
   * @param transducer a {@link com.trigram.zero.flow.Transducer} object
   * @return {@code ZeroFlow<E> }
   * @param <V> a V class
   * @param <E> a E class
   */
  default <V, E> ZeroFlow<E> windowedByTime(long timeMillis, Transducer<T, V, E> transducer) {

    return windowedByTime(timeMillis, transducer.reducer()).map(transducer.transformer());
  }

  /**
   * 转成int类型的成对流
   *
   * @param function a {@link java.util.function.ToIntFunction} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<IntPair<T>> withInt(ToIntFunction<T> function) {

    return map(t -> new IntPair<>(function.applyAsInt(t), t));
  }

  /**
   * <p>withDouble.</p>
   *
   * @param function a {@link java.util.function.ToDoubleFunction} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<DoublePair<T>> withDouble(ToDoubleFunction<T> function) {

    return map(t -> new DoublePair<>(function.applyAsDouble(t), t));
  }

  /**
   * <p>withLong.</p>
   *
   * @param function a {@link java.util.function.ToLongFunction} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<LongPair<T>> withLong(ToLongFunction<T> function) {

    return map(t -> new LongPair<>(function.applyAsLong(t), t));
  }

  /**
   * <p>withBool.</p>
   *
   * @param function a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<BooleanPair<T>> withBool(Predicate<T> function) {

    return map(t -> new BooleanPair<>(function.test(t), t));
  }

  /**
   * <p>withIndex.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<IntPair<T>> withIndex() {

    return c -> consumeIndexed((i, t) -> c.accept(new IntPair<>(i, t)));
  }

  /**
   * <p>zip.</p>
   *
   * @param bs a {@link java.lang.Iterable} object
   * @param cs a {@link java.lang.Iterable} object
   * @param <B> a B class
   * @param <C> a C class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <B, C> TripleZeroFlow<T, B, C> zip(Iterable<B> bs, Iterable<C> cs) {

    return c -> zip(bs, cs, c);
  }

  /**
   * <p>zip.</p>
   *
   * @param bs a {@link java.lang.Iterable} object
   * @param cs a {@link java.lang.Iterable} object
   * @param consumer a {@link com.trigram.zero.flow.triple.TripleConsumer} object
   * @param <B> a B class
   * @param <C> a C class
   */
  default <B, C> void zip(Iterable<B> bs, Iterable<C> cs, TripleConsumer<T, B, C> consumer) {

    Iterator<B> bi = bs.iterator();
    Iterator<C> ci = cs.iterator();
    consumeTillStop(t -> consumer.accept(t, ItrUtil.pop(bi), ItrUtil.pop(ci)));
  }

  /**
   * <p>zip.</p>
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <E> PairZeroFlow<T, E> zip(Iterable<E> iterable) {

    return c -> zip(iterable, c);
  }

  /**
   * <p>zip.</p>
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @param consumer a {@link java.util.function.BiConsumer} object
   * @param <E> a E class
   */
  default <E> void zip(Iterable<E> iterable, BiConsumer<T, E> consumer) {

    Iterator<E> iterator = iterable.iterator();
    consumeTillStop(t -> consumer.accept(t, ItrUtil.pop(iterator)));
  }

  /**
   * <p>zip.</p>
   *
   * @param list a {@link java.util.List} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<List<T>> zip(List<List<T>> list) {

    return c -> {

      ListZeroFlow<T> source = toList();
      int                  size   = source.size();
      for (int i = 0; i < size; i++) {
        ArrayListZeroFlow<T> des = new ArrayListZeroFlow<>(size);
        des.add(source.get(i));
        for (List<T> l : list) {
          des.add(l.get(i));
        }
        c.accept(des);
      }
    };
  }

  /**
   * 所有数据归为一个集合统一传递给下一个处理
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<List<T>> allToOne() {

    return c -> {
      ListZeroFlow<T> source = toList();
      c.accept(source);
    };
  }

  /**
   * <p>sizeOrDefault.</p>
   *
   * @return a int
   */
  default int sizeOrDefault() {

    return 10;
  }

  interface IntObjToInt<T> {

    int apply(int acc, T t);

  }

  interface DoubleObjToDouble<T> {

    double apply(double acc, T t);

  }

  interface LongObjToLong<T> {

    long apply(long acc, T t);

  }

  interface BooleanObjToBoolean<T> {

    boolean apply(boolean acc, T t);

  }

  interface IndexObjConsumer<T> {

    void accept(int i, T t);

  }

  interface IndexObjFunction<T, E> {

    E apply(int i, T t);

  }

  interface IndexObjPredicate<T> {

    boolean test(int i, T t);

  }

  class Empty {

    static final ZeroFlow<Object> emptySeq = c -> {
    };

    static final Consumer<Object> nothing = t -> {
    };

  }

}
