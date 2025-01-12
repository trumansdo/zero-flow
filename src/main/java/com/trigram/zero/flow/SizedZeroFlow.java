package com.trigram.zero.flow;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 可获取流中数据数量的流
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author Truma
 */
public interface SizedZeroFlow<T> extends ItrZeroFlow<T> {

  /** {@inheritDoc} */
  @Override
  default int sizeOrDefault() {

    return size();
  }

  /** {@inheritDoc} */
  @Override
  default int count() {

    return size();
  }

  /**
   * <p>isEmpty.</p>
   *
   * @return a boolean
   */
  boolean isEmpty();

  /**
   * {@inheritDoc}
   *
   * 当n超出容器最大下标用另一个替换处理，小于最大下标时的逻辑: {@link ZeroFlow#consume(Consumer, int, Consumer)}
   */
  @Override
  default void consume(Consumer<T> consumer, int n, Consumer<T> substitute) {

    if (n >= size()) {
      consume(substitute);
    } else {
      ItrZeroFlow.super.consume(consumer, n, substitute);
    }
  }

  /**
   * <p>size.</p>
   *
   * @return a int
   */
  int size();

  /** {@inheritDoc} */
  @Override
  default SizedZeroFlow<T> cache() {

    return this;
  }

  /**
   * <p>isNotEmpty.</p>
   *
   * @return a boolean
   */
  default boolean isNotEmpty() {

    return !isEmpty();
  }

  /**
   * {@inheritDoc}
   *
   * 删除前面多少个数据
   */
  @Override
  default ItrZeroFlow<T> drop(int n) {

    return n >= size() ? Collections::emptyIterator : ItrZeroFlow.super.drop(n);
  }

  /** {@inheritDoc} */
  @Override
  default <E> SizedZeroFlow<E> map(Function<T, E> function) {

    return new SizedZeroFlow<E>() {

      @Override
      public Iterator<E> iterator() {

        return ItrUtil.map(SizedZeroFlow.this.iterator(), function);
      }

      @Override
      public int size() {

        return SizedZeroFlow.this.size();
      }

      @Override
      public boolean isEmpty() {

        return SizedZeroFlow.this.isEmpty();
      }

      @Override
      public SizedZeroFlow<E> cache() {

        return toList();
      }
    };
  }

  /** {@inheritDoc} */
  @Override
  default <E> ItrZeroFlow<E> map(Function<T, E> function, int n, Function<T, E> substitute) {

    if (n >= size()) {
      return map(substitute);
    } else {
      return ItrZeroFlow.super.map(function, n, substitute);
    }
  }


  /** {@inheritDoc} */
  @Override
  default ItrZeroFlow<T> take(int n) {

    return n >= size() ? this : ItrZeroFlow.super.take(n);
  }


}
