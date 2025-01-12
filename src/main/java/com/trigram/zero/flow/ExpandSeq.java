package com.trigram.zero.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>ExpandSeq interface.</p>
 *
 * @author wolray
 */
public interface ExpandSeq<T> extends Function<T, ZeroFlow<T>> {

  /**
   * <p>of.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ExpandSeq} object
   */
  static <T> ExpandSeq<T> of(Function<T, ZeroFlow<T>> function) {

    return function instanceof ExpandSeq ? (ExpandSeq<T>) function : function::apply;
  }

  /**
   * <p>filter.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ExpandSeq} object
   */
  default ExpandSeq<T> filter(Predicate<T> predicate) {

    return t -> apply(t).filter(predicate);
  }

  /**
   * <p>filterNot.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ExpandSeq} object
   */
  default ExpandSeq<T> filterNot(Predicate<T> predicate) {

    return t -> apply(t).filter(predicate.negate());
  }

  /**
   * <p>scan.</p>
   *
   * @param c a {@link java.util.function.BiConsumer} object
   * @param node a T object
   */
  default void scan(BiConsumer<T, ListZeroFlow<T>> c, T node) {

    ListZeroFlow<T> sub = apply(node).filterNotNull().toList();
    c.accept(node, sub);
    sub.consume(n -> scan(c, n));
  }

  /**
   * <p>scan.</p>
   *
   * @param c a {@link java.util.function.Consumer} object
   * @param node a T object
   */
  default void scan(Consumer<T> c, T node) {

    c.accept(node);
    apply(node).consume(n -> {
      if (n != null) {
        scan(c, n);
      }
    });
  }

  /**
   * <p>scan.</p>
   *
   * @param c a {@link java.util.function.Consumer} object
   * @param node a T object
   * @param maxDepth a int
   * @param depth a int
   */
  default void scan(Consumer<T> c, T node, int maxDepth, int depth) {

    c.accept(node);
    if (depth < maxDepth) {
      apply(node).consume(n -> {
        if (n != null) {
          scan(c, n, maxDepth, depth + 1);
        }
      });
    }
  }

  /**
   * <p>terminate.</p>
   *
   * @param predicate a {@link java.util.function.Predicate} object
   * @return a {@link com.trigram.zero.flow.ExpandSeq} object
   */
  default ExpandSeq<T> terminate(Predicate<T> predicate) {

    return t -> predicate.test(t) ? ZeroFlow.empty() : apply(t);
  }

  /**
   * <p>toDAG.</p>
   *
   * @param nodes a {@link com.trigram.zero.flow.ZeroFlow} object
   * @return a {@link java.util.Map} object
   */
  default Map<T, ListZeroFlow<T>> toDAG(ZeroFlow<T> nodes) {

    Map<T, ListZeroFlow<T>> map = new HashMap<>();
    ExpandSeq<T>                 expand = terminate(t -> !map.containsKey(t));
    nodes.consume(t -> expand.scan(map::put, t));
    return map;
  }

  /**
   * <p>toDAG.</p>
   *
   * @param node a T object
   * @return a {@link java.util.Map} object
   */
  default Map<T, ListZeroFlow<T>> toDAG(T node) {

    Map<T, ListZeroFlow<T>> map = new HashMap<>();
    terminate(t -> !map.containsKey(t)).scan(map::put, node);
    return map;
  }

  /**
   * <p>toSeq.</p>
   *
   * @param node a T object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> toSeq(T node) {

    return c -> scan(c, node);
  }

  /**
   * <p>toSeq.</p>
   *
   * @param node a T object
   * @param maxDepth a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<T> toSeq(T node, int maxDepth) {

    return c -> scan(c, node, maxDepth, 0);
  }

}
