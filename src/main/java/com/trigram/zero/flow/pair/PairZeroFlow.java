package com.trigram.zero.flow.pair;

import com.trigram.zero.flow.BaseZeroFlow;
import com.trigram.zero.flow.ItrZeroFlow;
import com.trigram.zero.flow.MapZeroFlow;
import com.trigram.zero.flow.Mutable;
import com.trigram.zero.flow.Splitter;
import com.trigram.zero.flow.ZeroFlow;
import com.trigram.zero.flow.triple.TripleConsumer;
import com.trigram.zero.flow.triple.TripleFunction;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 二元流，对应天生的消费接口是{@link java.util.Map#forEach(BiConsumer)}
 *
 * @author wolray
 */
public interface PairZeroFlow<K, V> extends BaseZeroFlow<BiConsumer<K, V>> {

  /**
   * <p>empty.</p>
   *
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  @SuppressWarnings("unchecked")
  static <K, V> PairZeroFlow<K, V> empty() {

    return (PairZeroFlow<K, V>) Empty.emptySeq;
  }

  /**
   * <p>nothing.</p>
   *
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link java.util.function.BiConsumer} object
   */
  @SuppressWarnings("unchecked")
  static <K, V> BiConsumer<K, V> nothing() {

    return (BiConsumer<K, V>) Empty.nothing;
  }

  /**
   * <p>of.</p>
   *
   * @param map a {@link java.util.Map} object
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  static <K, V> PairZeroFlow<K, V> of(Map<K, V> map) {

    return map instanceof MapZeroFlow ? (MapZeroFlow<K, V>) map : map::forEach;
  }

  /**
   * <p>parseMap.</p>
   *
   * @see #parseMap(char[], char, char)
   * @param s a {@link java.lang.String} object
   * @param entrySep a char
   * @param kvSep a char
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  static PairZeroFlow<String, String> parseMap(String s, char entrySep, char kvSep) {

    return parseMap(s.toCharArray(), entrySep, kvSep);
  }

  /**
   * 将字符串解析为键值对的二元流。类似：key:val|key:val|key:val|key:val的字符串
   *
   * @param chars
   *     字符串
   * @param entrySep
   *     每个键值对之间的分隔符
   * @param kvSep
   *     键值的分隔符
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  static PairZeroFlow<String, String> parseMap(char[] chars, char entrySep, char kvSep) {

    return c -> {
      int    len  = chars.length, last = 0;
      String prev = null;
      for (int i = 0; i < len; i++) {
        if (chars[i] == entrySep) {
          if (prev != null) {
            c.accept(prev, Splitter.substring(chars, last, i));
            prev = null;
          }
          last = i + 1;
        } else if (prev == null && chars[i] == kvSep) {
          prev = Splitter.substring(chars, last, i);
          last = i + 1;
        }
      }
      if (prev != null) {
        c.accept(prev, Splitter.substring(chars, last, len));
      }
    };
  }

  /**
   * <p>unit.</p>
   *
   * @param k a K object
   * @param v a V object
   * @param <K> a K class
   * @param <V> a V class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  static <K, V> PairZeroFlow<K, V> unit(K k, V v) {

    return c -> c.accept(k, v);
  }

  /**
   * <p>cache.</p>
   *
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default PairZeroFlow<K, V> cache() {

    ZeroFlow<Pair<K, V>> pairSeq = paired().cache();
    return c -> pairSeq.consume(p -> c.accept(p.first, p.second));
  }

  /**
   * 将key和value用{@link com.trigram.zero.flow.pair.Pair}存放
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<Pair<K, V>> paired() {

    return map(Pair::new);
  }

  /**
   * <p>map.</p>
   *
   * @param function a {@link java.util.function.BiFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <T> ZeroFlow<T> map(BiFunction<K, V, T> function) {

    return c -> consume((k, v) -> c.accept(function.apply(k, v)));
  }

  /**
   * <p>filter.</p>
   *
   * @param predicate a {@link java.util.function.BiPredicate} object
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default PairZeroFlow<K, V> filter(BiPredicate<K, V> predicate) {

    return c -> consume((k, v) -> {
      if (predicate.test(k, v)) {
        c.accept(k, v);
      }
    });
  }

  /**
   * <p>filterByKey.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default PairZeroFlow<K, V> filterByKey(Predicate<K> predicate) {

    return c -> consume((k, v) -> {
      if (predicate.test(k)) {
        c.accept(k, v);
      }
    });
  }

  /**
   * <p>filterByValue.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default PairZeroFlow<K, V> filterByValue(Predicate<V> predicate) {

    return c -> consume((k, v) -> {
      if (predicate.test(v)) {
        c.accept(k, v);
      }
    });
  }

  /**
   * 折叠，如{@link com.trigram.zero.flow.ItrZeroFlow#fold(Object, BiFunction)}
   *
   * @return {@link E}
   * @param init a E object
   * @param function a {@link com.trigram.zero.flow.triple.TripleFunction} object
   * @param <E> a E class
   */
  default <E> E fold(E init, TripleFunction<E, K, V, E> function) {

    Mutable<E> m = new Mutable<>(init);
    consume((k, v) -> m.setIt(function.apply(m.getIt(), k, v)));
    return m.getIt();
  }

  /**
   * 返回key的流
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<K> justKeys() {

    return c -> consume((k, v) -> c.accept(k));
  }

  /**
   * 返回value的流
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<V> justValues() {

    return c -> consume((k, v) -> c.accept(v));
  }

  /**
   * <p>mapKey.</p>
   *
   * @param function a {@link java.util.function.BiFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <T> PairZeroFlow<T, V> mapKey(BiFunction<K, V, T> function) {

    return c -> consume((k, v) -> c.accept(function.apply(k, v), v));
  }

  /**
   * <p>mapKey.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <T> PairZeroFlow<T, V> mapKey(Function<K, T> function) {

    return c -> consume((k, v) -> c.accept(function.apply(k), v));
  }

  /**
   * <p>mapValue.</p>
   *
   * @param function a {@link java.util.function.BiFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <T> PairZeroFlow<K, T> mapValue(BiFunction<K, V, T> function) {

    return c -> consume((k, v) -> c.accept(k, function.apply(k, v)));
  }

  /**
   * <p>mapValue.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default <T> PairZeroFlow<K, T> mapValue(Function<V, T> function) {

    return c -> consume((k, v) -> c.accept(k, function.apply(v)));
  }

  /**
   * <p>maxByKey.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a {@link com.trigram.zero.flow.pair.Pair} object
   */
  default Pair<K, V> maxByKey(Comparator<K> comparator) {

    return reduce(new Pair<>(null, null), (p, k, v) -> {
      if (p.first == null || comparator.compare(p.first, k) < 0) {
        p.set(k, v);
      }
    });
  }

  /**
   * <p>reduce.</p>
   *
   * @param des a E object
   * @param accumulator a {@link com.trigram.zero.flow.triple.TripleConsumer} object
   * @param <E> a E class
   * @return a E object
   */
  default <E> E reduce(E des, TripleConsumer<E, K, V> accumulator) {

    consume((k, v) -> accumulator.accept(des, k, v));
    return des;
  }

  /**
   * <p>maxByValue.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a {@link com.trigram.zero.flow.pair.Pair} object
   */
  default Pair<K, V> maxByValue(Comparator<V> comparator) {

    return reduce(new Pair<>(null, null), (p, k, v) -> {
      if (p.second == null || comparator.compare(p.second, v) < 0) {
        p.set(k, v);
      }
    });
  }

  /**
   * <p>minByKey.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a {@link com.trigram.zero.flow.pair.Pair} object
   */
  default Pair<K, V> minByKey(Comparator<K> comparator) {

    return reduce(new Pair<>(null, null), (p, k, v) -> {
      if (p.first == null || comparator.compare(p.first, k) > 0) {
        p.set(k, v);
      }
    });
  }

  /**
   * <p>minByValue.</p>
   *
   * @param comparator a {@link java.util.Comparator} object
   * @return a {@link com.trigram.zero.flow.pair.Pair} object
   */
  default Pair<K, V> minByValue(Comparator<V> comparator) {

    return reduce(new Pair<>(null, null), (p, k, v) -> {
      if (p.second == null || comparator.compare(p.second, v) > 0) {
        p.set(k, v);
      }
    });
  }

  /**
   * <p>onEach.</p>
   *
   * @param consumer a {@link java.util.function.BiConsumer} object
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default PairZeroFlow<K, V> onEach(BiConsumer<K, V> consumer) {

    return c -> consumeTillStop(consumer.andThen(c));
  }

  /**
   * 交换key和value
   *
   * @return a {@link com.trigram.zero.flow.pair.PairZeroFlow} object
   */
  default PairZeroFlow<V, K> swap() {

    return c -> consume((k, v) -> c.accept(v, k));
  }

  /**
   * <p>toMap.</p>
   *
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default MapZeroFlow<K, V> toMap() {

    return toMap(new LinkedHashMap<>());
  }

  /**
   * <p>toMap.</p>
   *
   * @param des a {@link java.util.Map} object
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default MapZeroFlow<K, V> toMap(Map<K, V> des) {

    return MapZeroFlow.of(reduce(des, Map::put));
  }

  /**
   * <p>toMap.</p>
   *
   * @param toKey a {@link java.util.function.BiFunction} object
   * @param toValue a {@link java.util.function.BiFunction} object
   * @param <A> a A class
   * @param <B> a B class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <A, B> MapZeroFlow<A, B> toMap(BiFunction<K, V, A> toKey, BiFunction<K, V, B> toValue) {

    return toMap(new LinkedHashMap<>(), toKey, toValue);
  }

  /**
   * <p>toMap.</p>
   *
   * @param des a {@link java.util.Map} object
   * @param toKey a {@link java.util.function.BiFunction} object
   * @param toValue a {@link java.util.function.BiFunction} object
   * @param <A> a A class
   * @param <B> a B class
   * @return a {@link com.trigram.zero.flow.MapZeroFlow} object
   */
  default <A, B> MapZeroFlow<A, B> toMap(Map<A, B> des, BiFunction<K, V, A> toKey, BiFunction<K, V, B> toValue) {

    return MapZeroFlow.of(reduce(des, (res, k, v) -> res.put(toKey.apply(k, v), toValue.apply(k, v))));
  }

  class Empty {

    static final PairZeroFlow<Object, Object> emptySeq = c -> {
    };

    static final BiConsumer<Object, Object> nothing = (k, v) -> {
    };

  }

}
