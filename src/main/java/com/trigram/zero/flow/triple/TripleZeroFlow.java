package com.trigram.zero.flow.triple;

import com.trigram.zero.flow.BaseZeroFlow;
import com.trigram.zero.flow.ZeroFlow;
import java.util.function.Function;

/**
 * 三元流，暂时无对应的
 *
 * @author wolray
 */
public interface TripleZeroFlow<A, B, C> extends BaseZeroFlow<TripleConsumer<A, B, C>> {

  /**
   * <p>empty.</p>
   *
   * @param <A> a A class
   * @param <B> a B class
   * @param <C> a C class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  @SuppressWarnings("unchecked")
  static <A, B, C> TripleZeroFlow<A, B, C> empty() {

    return (TripleZeroFlow<A, B, C>) Empty.emptySeq;
  }

  /**
   * <p>nothing.</p>
   *
   * @param <A> a A class
   * @param <B> a B class
   * @param <C> a C class
   * @return a {@link com.trigram.zero.flow.triple.TripleConsumer} object
   */
  @SuppressWarnings("unchecked")
  static <A, B, C> TripleConsumer<A, B, C> nothing() {

    return (TripleConsumer<A, B, C>) Empty.nothing;
  }

  /**
   * <p>filter.</p>
   *
   * @param predicate a {@link com.trigram.zero.flow.triple.TripleZeroFlow.TriPredicate} object
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default TripleZeroFlow<A, B, C> filter(TriPredicate<A, B, C> predicate) {

    return cs -> consume((a, b, c) -> {
      if (predicate.test(a, b, c)) {
        cs.accept(a, b, c);
      }
    });
  }

  /**
   * <p>first.</p>
   *
   * @return a {@link com.trigram.zero.flow.triple.Triple} object
   */
  default Triple<A, B, C> first() {

    Triple<A, B, C> t = new Triple<>(null, null, null);
    consumeTillStop((a, b, c) -> {
      t.first  = a;
      t.second = b;
      t.third  = c;
      ZeroFlow.stop();
    });
    return t;
  }

  /**
   * <p>keepFirst.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<A> keepFirst() {

    return cs -> consume((a, b, c) -> cs.accept(a));
  }

  /**
   * <p>keepSecond.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<B> keepSecond() {

    return cs -> consume((a, b, c) -> cs.accept(b));
  }

  /**
   * <p>keepThird.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<C> keepThird() {

    return cs -> consume((a, b, c) -> cs.accept(c));
  }

  /**
   * <p>mapFirst.</p>
   *
   * @param function a {@link com.trigram.zero.flow.triple.TripleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <T> TripleZeroFlow<T, B, C> mapFirst(TripleFunction<A, B, C, T> function) {

    return cs -> consume((a, b, c) -> cs.accept(function.apply(a, b, c), b, c));
  }

  /**
   * <p>mapFirst.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <T> TripleZeroFlow<T, B, C> mapFirst(Function<A, T> function) {

    return cs -> consume((a, b, c) -> cs.accept(function.apply(a), b, c));
  }

  /**
   * <p>mapSecond.</p>
   *
   * @param function a {@link com.trigram.zero.flow.triple.TripleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <T> TripleZeroFlow<A, T, C> mapSecond(TripleFunction<A, B, C, T> function) {

    return cs -> consume((a, b, c) -> cs.accept(a, function.apply(a, b, c), c));
  }

  /**
   * <p>mapSecond.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <T> TripleZeroFlow<A, T, C> mapSecond(Function<B, T> function) {

    return cs -> consume((a, b, c) -> cs.accept(a, function.apply(b), c));
  }

  /**
   * <p>mapThird.</p>
   *
   * @param function a {@link com.trigram.zero.flow.triple.TripleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <T> TripleZeroFlow<A, B, T> mapThird(TripleFunction<A, B, C, T> function) {

    return cs -> consume((a, b, c) -> cs.accept(a, b, function.apply(a, b, c)));
  }

  /**
   * <p>mapThird.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.triple.TripleZeroFlow} object
   */
  default <T> TripleZeroFlow<A, B, T> mapThird(Function<C, T> function) {

    return cs -> consume((a, b, c) -> cs.accept(a, b, function.apply(c)));
  }

  /**
   * <p>tripled.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<Triple<A, B, C>> tripled() {

    return map(Triple::new);
  }

  /**
   * <p>map.</p>
   *
   * @param function a {@link com.trigram.zero.flow.triple.TripleFunction} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <T> ZeroFlow<T> map(TripleFunction<A, B, C, T> function) {

    return cs -> consume((a, b, c) -> cs.accept(function.apply(a, b, c)));
  }

  interface TriPredicate<A, B, D> {

    boolean test(A a, B b, D d);

  }

  class Empty {

    static final TripleZeroFlow<Object, Object, Object> emptySeq = cs -> {
    };

    static final TripleConsumer<Object, Object, Object> nothing = (a, b, c) -> {
    };

  }

}
