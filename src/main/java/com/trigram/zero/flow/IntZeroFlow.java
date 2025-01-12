package com.trigram.zero.flow;

import com.trigram.zero.flow.pair.IntPair;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;

/**
 * <p>IntZeroFlow interface.</p>
 *
 * @author wolray
 */
public interface IntZeroFlow extends BaseZeroFlow<IntConsumer> {

  IntZeroFlow empty = c -> {
  /** Constant <code>empty</code> */
  };

  IntConsumer nothing = t -> {
  /** Constant <code>nothing</code> */
  };

  /**
   * <p>gen.</p>
   *
   * @param seed a int
   * @param operator a {@link java.util.function.IntUnaryOperator} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow gen(int seed, IntUnaryOperator operator) {

    return c -> {
      int t = seed;
      c.accept(t);
      while (true) {
        c.accept(t = operator.applyAsInt(t));
      }
    };
  }

  /**
   * <p>gen.</p>
   *
   * @param seed1 a int
   * @param seed2 a int
   * @param operator a {@link java.util.function.IntBinaryOperator} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow gen(int seed1, int seed2, IntBinaryOperator operator) {

    return c -> {
      int t1 = seed1, t2 = seed2;
      c.accept(t1);
      c.accept(t2);
      while (true) {
        c.accept(t2 = operator.applyAsInt(t1, t1 = t2));
      }
    };
  }

  /**
   * <p>gen.</p>
   *
   * @param supplier a {@link java.util.function.IntSupplier} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow gen(IntSupplier supplier) {

    return c -> {
      while (true) {
        c.accept(supplier.getAsInt());
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param cs a {@link java.lang.CharSequence} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow of(CharSequence cs) {

    return c -> {
      for (int i = 0; i < cs.length(); i++) {
        c.accept(cs.charAt(i));
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param ts a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow of(int... ts) {

    return c -> {
      for (int t : ts) {
        c.accept(t);
      }
    };
  }

  /**
   * <p>range.</p>
   *
   * @param start a int
   * @param stop a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow range(int start, int stop) {

    return range(start, stop, 1);
  }

  /**
   * <p>range.</p>
   *
   * @param start a int
   * @param stop a int
   * @param step a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow range(int start, int stop, int step) {

    if (step == 0) {
      throw new IllegalArgumentException("step is 0");
    }
    return c -> {
      if (step > 0) {
        for (int i = start; i < stop; i += step) {
          c.accept(i);
        }
      } else {
        for (int i = start; i > stop; i += step) {
          c.accept(i);
        }
      }
    };
  }

  /**
   * <p>range.</p>
   *
   * @param stop a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow range(int stop) {

    return range(0, stop, 1);
  }

  /**
   * <p>repeat.</p>
   *
   * @param n a int
   * @param value a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  static IntZeroFlow repeat(int n, int value) {

    return c -> {
      for (int i = 0; i < n; i++) {
        c.accept(value);
      }
    };
  }

  /**
   * <p>all.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a boolean
   */
  default boolean all(IntPredicate predicate) {

    return !find(predicate.negate()).isPresent();
  }

  /**
   * <p>find.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link java.util.OptionalInt} object
   */
  default OptionalInt find(IntPredicate predicate) {

    Mutable<Integer> m = new Mutable<>(null);
    consumeTillStop(t -> {
      if (predicate.test(t)) {
        m.set(t);
        ZeroFlow.stop();
      }
    });
    return m.isSet ? OptionalInt.of(m.it) : OptionalInt.empty();
  }

  /**
   * <p>anyNot.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a boolean
   */
  default boolean anyNot(IntPredicate predicate) {

    return any(predicate.negate());
  }

  /**
   * <p>any.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a boolean
   */
  default boolean any(IntPredicate predicate) {

    return find(predicate).isPresent();
  }

  /**
   * <p>append.</p>
   *
   * @param t a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow append(int t) {

    return c -> {
      consume(c);
      c.accept(t);
    };
  }

  /**
   * <p>append.</p>
   *
   * @param t a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow append(int... t) {

    return c -> {
      consume(c);
      for (int x : t) {
        c.accept(x);
      }
    };
  }

  /**
   * <p>appendWith.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.IntZeroFlow} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow appendWith(IntZeroFlow seq) {

    return c -> {
      consume(c);
      seq.consume(c);
    };
  }

  /**
   * <p>average.</p>
   *
   * @return a double
   */
  default double average() {

    return average(null);
  }

  /**
   * <p>average.</p>
   *
   * @param weightFunction a {@link java.util.function.IntToDoubleFunction} object
   * @return a double
   */
  default double average(IntToDoubleFunction weightFunction) {

    double[] a = {0, 0};
    consume(t -> {
      if (weightFunction != null) {
        double w = weightFunction.applyAsDouble(t);
        a[0] += t * w;
        a[1] += w;
      } else {
        a[0] += t;
        a[1] += 1;
      }
    });
    return a[1] != 0 ? a[0] / a[1] : 0;
  }

  /**
   * <p>boxed.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<Integer> boxed() {

    return c -> consume(c::accept);
  }

  /**
   * <p>circle.</p>
   *
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow circle() {

    return c -> {
      while (true) {
        consume(c);
      }
    };
  }

  /**
   * <p>consumeIndexedTillStop.</p>
   *
   * @param consumer a {@link com.trigram.zero.flow.IntZeroFlow.IndexIntConsumer} object
   */
  default void consumeIndexedTillStop(IndexIntConsumer consumer) {

    int[] a = {0};
    consumeTillStop(t -> consumer.accept(a[0]++, t));
  }

  /**
   * <p>count.</p>
   *
   * @return a int
   */
  default int count() {

    return reduce(new int[1], (a, t) -> a[0]++)[0];
  }

  /**
   * <p>reduce.</p>
   *
   * @param des a E object
   * @param consumer a {@link com.trigram.zero.flow.IntZeroFlow.ObjIntConsumer} object
   * @param <E> a E class
   * @return a E object
   */
  default <E> E reduce(E des, ObjIntConsumer<E> consumer) {

    consume(t -> consumer.accept(des, t));
    return des;
  }

  /**
   * <p>countNot.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a int
   */
  default int countNot(IntPredicate predicate) {

    return count(predicate.negate());
  }

  /**
   * <p>count.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a int
   */
  default int count(IntPredicate predicate) {

    return reduce(new int[1], (a, t) -> {
      if (predicate.test(t)) {
        a[0]++;
      }
    })[0];
  }

  /**
   * <p>distinct.</p>
   *
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow distinct() {

    return distinctBy(i -> i);
  }

  /**
   * <p>distinctBy.</p>
   *
   * @param function a {@link java.util.function.IntFunction} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default <E> IntZeroFlow distinctBy(IntFunction<E> function) {

    return c -> reduce(new HashSet<>(), (set, t) -> {
      if (set.add(function.apply(t))) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>drop.</p>
   *
   * @param n a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow drop(int n) {

    return n <= 0 ? this : partial(n, nothing);
  }

  /**
   * <p>partial.</p>
   *
   * @param n a int
   * @param substitute a {@link java.util.function.IntConsumer} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow partial(int n, IntConsumer substitute) {

    return c -> consume(c, n, substitute);
  }

  /**
   * <p>consume.</p>
   *
   * @param consumer a {@link java.util.function.IntConsumer} object
   * @param n a int
   * @param substitute a {@link java.util.function.IntConsumer} object
   */
  default void consume(IntConsumer consumer, int n, IntConsumer substitute) {

    if (n > 0) {
      int[] a = {n - 1};
      consume(t -> {
        if (a[0] < 0) {
          consumer.accept(t);
        } else {
          a[0]--;
          substitute.accept(t);
        }
      });
    } else {
      consume(consumer);
    }
  }

  /**
   * <p>dropWhile.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow dropWhile(IntPredicate predicate) {

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
   * @param function a {@link com.trigram.zero.flow.IntZeroFlow.BoolIntToBool} object
   * @return a boolean
   */
  default boolean foldBoolean(boolean init, BoolIntToBool function) {

    boolean[] a = {init};
    consume(i -> a[0] = function.apply(a[0], i));
    return a[0];
  }

  /**
   * <p>duplicateAll.</p>
   *
   * @param times a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow duplicateAll(int times) {

    return c -> {
      for (int i = 0; i < times; i++) {
        consume(c);
      }
    };
  }

  /**
   * <p>duplicateEach.</p>
   *
   * @param times a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow duplicateEach(int times) {

    return c -> consume(t -> {
      for (int i = 0; i < times; i++) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>duplicateIf.</p>
   *
   * @param times a int
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow duplicateIf(int times, IntPredicate predicate) {

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
   * <p>filter.</p>
   *
   * @param n a int
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow filter(int n, IntPredicate predicate) {

    return c -> consume(c, n, t -> {
      if (predicate.test(t)) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>filterIndexed.</p>
   *
   * @param predicate a {@link com.trigram.zero.flow.IntZeroFlow.IndexIntPredicate} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow filterIndexed(IndexIntPredicate predicate) {

    return c -> consumeIndexed((i, t) -> {
      if (predicate.test(i, t)) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>consumeIndexed.</p>
   *
   * @param consumer a {@link com.trigram.zero.flow.IntZeroFlow.IndexIntConsumer} object
   */
  default void consumeIndexed(IndexIntConsumer consumer) {

    int[] a = {0};
    consume(t -> consumer.accept(a[0]++, t));
  }

  /**
   * <p>filterNot.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow filterNot(IntPredicate predicate) {

    return filter(predicate.negate());
  }

  /**
   * <p>filter.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow filter(IntPredicate predicate) {

    return c -> consume(t -> {
      if (predicate.test(t)) {
        c.accept(t);
      }
    });
  }

  /**
   * <p>findNot.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link java.util.OptionalInt} object
   */
  default OptionalInt findNot(IntPredicate predicate) {

    return find(predicate.negate());
  }

  /**
   * <p>first.</p>
   *
   * @return a {@link java.util.OptionalInt} object
   */
  default OptionalInt first() {

    return find(t -> true);
  }

  /**
   * <p>flatMap.</p>
   *
   * @param function a {@link java.util.function.IntFunction} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow flatMap(IntFunction<IntZeroFlow> function) {

    return c -> consume(t -> function.apply(t).consume(c));
  }

  /**
   * <p>foldDouble.</p>
   *
   * @param init a double
   * @param function a {@link com.trigram.zero.flow.IntZeroFlow.DoubleIntToDouble} object
   * @return a double
   */
  default double foldDouble(double init, DoubleIntToDouble function) {

    double[] a = {init};
    consume(i -> a[0] = function.apply(a[0], i));
    return a[0];
  }

  /**
   * <p>foldLong.</p>
   *
   * @param init a long
   * @param function a {@link com.trigram.zero.flow.IntZeroFlow.LongIntToLong} object
   * @return a long
   */
  default long foldLong(long init, LongIntToLong function) {

    long[] a = {init};
    consume(i -> a[0] = function.apply(a[0], i));
    return a[0];
  }

  /**
   * <p>lastNot.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link java.util.OptionalInt} object
   */
  default OptionalInt lastNot(IntPredicate predicate) {

    return last(predicate.negate());
  }

  /**
   * <p>last.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link java.util.OptionalInt} object
   */
  default OptionalInt last(IntPredicate predicate) {

    return filter(predicate).last();
  }

  /**
   * <p>last.</p>
   *
   * @return a {@link java.util.OptionalInt} object
   */
  default OptionalInt last() {

    Mutable<Integer> m = new Mutable<>(null);
    consume(m::set);
    return m.isSet ? OptionalInt.of(m.it) : OptionalInt.empty();
  }

  /**
   * <p>map.</p>
   *
   * @param function a {@link java.util.function.IntUnaryOperator} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow map(IntUnaryOperator function) {

    return c -> consume(t -> c.accept(function.applyAsInt(t)));
  }

  /**
   * <p>mapIndexed.</p>
   *
   * @param function a {@link com.trigram.zero.flow.IntZeroFlow.IndexIntToInt} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow mapIndexed(IndexIntToInt function) {

    return c -> consumeIndexed((i, t) -> c.accept(function.apply(i, t)));
  }

  /**
   * <p>mapToObj.</p>
   *
   * @param function a {@link java.util.function.IntFunction} object
   * @param n a int
   * @param substitute a {@link java.util.function.IntFunction} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> mapToObj(IntFunction<E> function, int n, IntFunction<E> substitute) {

    return n <= 0 ? mapToObj(function) : c -> {
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
   * <p>mapToObj.</p>
   *
   * @param function a {@link java.util.function.IntFunction} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> mapToObj(IntFunction<E> function) {

    return c -> consume(t -> c.accept(function.apply(t)));
  }

  /**
   * <p>max.</p>
   *
   * @return a {@link java.lang.Integer} object
   */
  default Integer max() {

    return fold(null, (f, t) -> f == null || f < t ? t : f);
  }

  /**
   * <p>fold.</p>
   *
   * @param init a E object
   * @param function a {@link com.trigram.zero.flow.IntZeroFlow.ObjIntToObj} object
   * @param <E> a E class
   * @return a E object
   */
  default <E> E fold(E init, ObjIntToObj<E> function) {

    Mutable<E> m = new Mutable<>(init);
    consume(t -> m.it = function.apply(m.it, t));
    return m.it;
  }

  /**
   * <p>max.</p>
   *
   * @param function a {@link java.util.function.IntFunction} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.pair.IntPair} object
   */
  default <V extends Comparable<V>> IntPair<V> max(IntFunction<V> function) {

    return reduce(new IntPair<>(0, null), (p, t) -> {
      V v = function.apply(t);
      if (p.second == null || p.second.compareTo(v) < 0) {
        p.first  = t;
        p.second = v;
      }
    });
  }

  /**
   * <p>min.</p>
   *
   * @return a {@link java.lang.Integer} object
   */
  default Integer min() {

    return fold(null, (f, t) -> f == null || f > t ? t : f);
  }

  /**
   * <p>min.</p>
   *
   * @param function a {@link java.util.function.IntFunction} object
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.pair.IntPair} object
   */
  default <V extends Comparable<V>> IntPair<V> min(IntFunction<V> function) {

    return reduce(new IntPair<>(0, null), (p, t) -> {
      V v = function.apply(t);
      if (p.second == null || p.second.compareTo(v) > 0) {
        p.first  = t;
        p.second = v;
      }
    });
  }

  /**
   * <p>none.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a boolean
   */
  default boolean none(IntPredicate predicate) {

    return !find(predicate).isPresent();
  }

  /**
   * <p>onEach.</p>
   *
   * @param n a int
   * @param consumer a {@link java.util.function.IntConsumer} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow onEach(int n, IntConsumer consumer) {

    return c -> consume(c, n, consumer.andThen(c));
  }

  /**
   * <p>onEach.</p>
   *
   * @param consumer a {@link java.util.function.IntConsumer} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow onEach(IntConsumer consumer) {

    return c -> consume(consumer.andThen(c));
  }

  /**
   * <p>onEachIndexed.</p>
   *
   * @param consumer a {@link com.trigram.zero.flow.IntZeroFlow.IndexIntConsumer} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow onEachIndexed(IndexIntConsumer consumer) {

    return c -> consumeIndexed((i, t) -> {
      consumer.accept(i, t);
      c.accept(t);
    });
  }

  /**
   * <p>replace.</p>
   *
   * @param n a int
   * @param operator a {@link java.util.function.IntUnaryOperator} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow replace(int n, IntUnaryOperator operator) {

    return c -> consume(c, n, t -> c.accept(operator.applyAsInt(t)));
  }

  /**
   * <p>runningFold.</p>
   *
   * @return {@link com.trigram.zero.flow.IntZeroFlow}
   * @see ItrZeroFlow#fold(Object, BiFunction)
   * @param init a int
   * @param function a {@link java.util.function.IntBinaryOperator} object
   */
  default IntZeroFlow runningFold(int init, IntBinaryOperator function) {

    return c -> foldInt(init, (acc, t) -> {
      acc = function.applyAsInt(acc, t);
      c.accept(acc);
      return acc;
    });
  }

  /**
   * <p>foldInt.</p>
   *
   * @param init a int
   * @param function a {@link java.util.function.IntBinaryOperator} object
   * @return a int
   */
  default int foldInt(int init, IntBinaryOperator function) {

    int[] a = {init};
    consume(i -> a[0] = function.applyAsInt(a[0], i));
    return a[0];
  }

  /**
   * <p>sum.</p>
   *
   * @return a int
   */
  default int sum() {

    return reduce(new int[1], (a, t) -> a[0] += t)[0];
  }

  /**
   * <p>sum.</p>
   *
   * @param function a {@link java.util.function.IntUnaryOperator} object
   * @return a int
   */
  default int sum(IntUnaryOperator function) {

    return reduce(new int[1], (a, t) -> a[0] += function.applyAsInt(t))[0];
  }

  /**
   * <p>take.</p>
   *
   * @param n a int
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow take(int n) {

    return n <= 0 ? empty : c -> {
      int[] i = {n};
      consumeTillStop(t -> {
        if (i[0]-- > 0) {
          c.accept(t);
        } else {
          ZeroFlow.stop();
        }
      });
    };
  }

  /**
   * <p>takeWhile.</p>
   *
   * @param predicate a {@link java.util.function.IntPredicate} object
   * @return a {@link com.trigram.zero.flow.IntZeroFlow} object
   */
  default IntZeroFlow takeWhile(IntPredicate predicate) {

    return c -> consumeTillStop(t -> {
      if (predicate.test(t)) {
        c.accept(t);
      } else {
        ZeroFlow.stop();
      }
    });
  }

  /**
   * <p>toArray.</p>
   *
   * @return an array of {@link int} objects
   */
  default int[] toArray() {

    return toBatched().toArray();
  }

  /**
   * <p>toBatched.</p>
   *
   * @return a {@link com.trigram.zero.flow.IntZeroFlow.Batched} object
   */
  default Batched toBatched() {

    return reduce(new Batched(), Batched::add);
  }

  interface ObjIntConsumer<E> {

    void accept(E e, int i);

  }

  interface ObjIntToObj<E> {

    E apply(E e, int t);

  }

  interface DoubleIntToDouble {

    long apply(double acc, int t);

  }

  interface LongIntToLong {

    long apply(long acc, int t);

  }

  interface BoolIntToBool {

    boolean apply(boolean acc, int t);

  }

  interface IndexIntConsumer {

    void accept(int i, int t);

  }

  interface IndexIntPredicate {

    boolean test(int i, int t);

  }

  interface IndexIntToInt {

    int apply(int i, int t);

  }

  class Batched implements IntZeroFlow {

    private final LinkedList<int[]> list = new LinkedList<>();

    public int size;

    private int batchSize = 10;

    private int[] cur;

    private int index;

    @Override
    public void consume(IntConsumer consumer) {

      list.forEach(a -> {
        for (int i = 0, size = sizeOf(a); i < size; i++) {
          consumer.accept(a[i]);
        }
      });
    }

    private int sizeOf(int[] a) {

      return a != cur ? a.length : index;
    }

    @Override
    public int[] toArray() {

      int[] a   = new int[size];
      int   pos = 0;
      for (int[] sub : list) {
        System.arraycopy(sub, 0, a, pos, sizeOf(sub));
        pos += sub.length;
      }
      return a;
    }

    public void add(int t) {

      if (cur == null) {
        cur = new int[batchSize];
        list.add(cur);
        index = 0;
      }
      cur[index++] = t;
      size++;
      if (index == batchSize) {
        cur       = null;
        batchSize = Math.min(300, Math.max(batchSize, size >> 1));
      }
    }

  }

}
