package com.trigram.zero.flow;

import com.trigram.zero.flow.triple.TripleFunction;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 延迟获取存放数据接口，扩展{@link java.util.function.Supplier}接口
 *
 * @version 1.0.0
 * @since 1.0.0
 * @author Truma
 */
public interface Lazy<T> extends Supplier<T> {

  /**
   * <p>of.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <T> Lazy<T> of(ZeroFlow<T> seq) {

    return seq.lazyLast();
  }

  /**
   * <p>of.</p>
   *
   * @param s1 a {@link java.util.function.Supplier} object
   * @param s2 a {@link java.util.function.Supplier} object
   * @param function a {@link java.util.function.BiFunction} object
   * @param <A> a A class
   * @param <B> a B class
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <A, B, T> Lazy<T> of(Supplier<A> s1, Supplier<B> s2, BiFunction<A, B, T> function) {

    return new Mutable<T>(null) {

      @Override
      protected void eval() {

        it = function.apply(s1.get(), s2.get());
      }

      @Override
      protected void eval(ForkJoinPool pool) {

        Supplier<A> a = submit(pool, s1);
        Supplier<B> b = submit(pool, s2);
        it = function.apply(a.get(), b.get());
      }
    };
  }

  /**
   * <p>submit.</p>
   *
   * @param pool a {@link java.util.concurrent.ForkJoinPool} object
   * @param supplier a {@link java.util.function.Supplier} object
   * @param <T> a T class
   * @return a {@link java.util.function.Supplier} object
   */
  static <T> Supplier<T> submit(ForkJoinPool pool, Supplier<T> supplier) {

    RecursiveTask<T> task;
    if (supplier instanceof Lazy) {
      Lazy<T> lazy = (Lazy<T>) supplier;
      if (lazy.isSet()) {
        return lazy;
      } else {
        task = new RecursiveTask<T>() {

          @Override
          protected T compute() {

            return lazy.forkJoin(pool);
          }
        };
      }
    } else {
      task = new RecursiveTask<T>() {

        @Override
        protected T compute() {

          return supplier.get();
        }
      };
    }
    pool.submit(task);
    return task::join;
  }

  /**
   * <p>of.</p>
   *
   * @param s1 a {@link java.util.function.Supplier} object
   * @param s2 a {@link java.util.function.Supplier} object
   * @param s3 a {@link java.util.function.Supplier} object
   * @param function a {@link com.trigram.zero.flow.triple.TripleFunction} object
   * @param <A> a A class
   * @param <B> a B class
   * @param <C> a C class
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <A, B, C, T> Lazy<T> of(Supplier<A> s1, Supplier<B> s2, Supplier<C> s3, TripleFunction<A, B, C, T> function) {

    return new Mutable<T>(null) {

      @Override
      protected void eval() {

        it = function.apply(s1.get(), s2.get(), s3.get());
      }

      @Override
      protected void eval(ForkJoinPool pool) {

        Supplier<A> u = submit(pool, s1);
        Supplier<B> v = submit(pool, s2);
        Supplier<C> w = submit(pool, s3);
        it = function.apply(u.get(), v.get(), w.get());
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param s1 a {@link java.util.function.Supplier} object
   * @param s2 a {@link java.util.function.Supplier} object
   * @param s3 a {@link java.util.function.Supplier} object
   * @param s4 a {@link java.util.function.Supplier} object
   * @param function a {@link com.trigram.zero.flow.FourFunction} object
   * @param <A> a A class
   * @param <B> a B class
   * @param <C> a C class
   * @param <D> a D class
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <A, B, C, D, T> Lazy<T> of(
      Supplier<A> s1, Supplier<B> s2, Supplier<C> s3, Supplier<D> s4, FourFunction<A, B, C, D, T> function
  ) {

    return new Mutable<T>(null) {

      @Override
      protected void eval() {

        it = function.apply(s1.get(), s2.get(), s3.get(), s4.get());
      }

      @Override
      protected void eval(ForkJoinPool pool) {

        Supplier<A> a = submit(pool, s1);
        Supplier<B> b = submit(pool, s2);
        Supplier<C> c = submit(pool, s3);
        Supplier<D> d = submit(pool, s4);
        it = function.apply(a.get(), b.get(), c.get(), d.get());
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param s1 a {@link java.util.function.Supplier} object
   * @param s2 a {@link java.util.function.Supplier} object
   * @param s3 a {@link java.util.function.Supplier} object
   * @param s4 a {@link java.util.function.Supplier} object
   * @param s5 a {@link java.util.function.Supplier} object
   * @param function a {@link com.trigram.zero.flow.FiveFunction} object
   * @param <A> a A class
   * @param <B> a B class
   * @param <C> a C class
   * @param <D> a D class
   * @param <E> a E class
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <A, B, C, D, E, T> Lazy<T> of(
      Supplier<A> s1, Supplier<B> s2, Supplier<C> s3, Supplier<D> s4, Supplier<E> s5,
      FiveFunction<A, B, C, D, E, T> function
  ) {

    return new Mutable<T>(null) {

      @Override
      protected void eval() {

        it = function.apply(s1.get(), s2.get(), s3.get(), s4.get(), s5.get());
      }

      @Override
      protected void eval(ForkJoinPool pool) {

        Supplier<A> a = submit(pool, s1);
        Supplier<B> b = submit(pool, s2);
        Supplier<C> c = submit(pool, s3);
        Supplier<D> d = submit(pool, s4);
        Supplier<E> e = submit(pool, s5);
        it = function.apply(a.get(), b.get(), c.get(), d.get(), e.get());
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param s1 a {@link java.util.function.Supplier} object
   * @param s2 a {@link java.util.function.Supplier} object
   * @param s3 a {@link java.util.function.Supplier} object
   * @param s4 a {@link java.util.function.Supplier} object
   * @param s5 a {@link java.util.function.Supplier} object
   * @param s6 a {@link java.util.function.Supplier} object
   * @param function a {@link com.trigram.zero.flow.SixFunction} object
   * @param <A> a A class
   * @param <B> a B class
   * @param <C> a C class
   * @param <D> a D class
   * @param <E> a E class
   * @param <F> a F class
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <A, B, C, D, E, F, T> Lazy<T> of(
      Supplier<A> s1, Supplier<B> s2, Supplier<C> s3, Supplier<D> s4, Supplier<E> s5, Supplier<F> s6,
      SixFunction<A, B, C, D, E, F, T> function
  ) {

    return new Mutable<T>(null) {

      @Override
      protected void eval() {

        it = function.apply(s1.get(), s2.get(), s3.get(), s4.get(), s5.get(), s6.get());
      }

      @Override
      protected void eval(ForkJoinPool pool) {

        Supplier<A> a = submit(pool, s1);
        Supplier<B> b = submit(pool, s2);
        Supplier<C> c = submit(pool, s3);
        Supplier<D> d = submit(pool, s4);
        Supplier<E> e = submit(pool, s5);
        Supplier<F> f = submit(pool, s6);
        it = function.apply(a.get(), b.get(), c.get(), d.get(), e.get(), f.get());
      }
    };
  }

  /**
   * <p>unset.</p>
   *
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <T> Lazy<T> unset() {

    return of(() -> {
      throw new UnsetException();
    });
  }

  /**
   * <p>of.</p>
   *
   * @param supplier a {@link java.util.function.Supplier} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <T> Lazy<T> of(Supplier<T> supplier) {

    if (supplier instanceof Lazy) {
      return (Lazy<T>) supplier;
    }
    return new Mutable<T>(null) {

      @Override
      protected void eval() {

        it = supplier.get();
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param s1 a {@link java.util.function.Supplier} object
   * @param function a {@link java.util.function.Function} object
   * @param <A> a A class
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  static <A, T> Lazy<T> of(Supplier<A> s1, Function<A, T> function) {

    return of(() -> function.apply(s1.get()));
  }

  /**
   * <p>isSet.</p>
   *
   * @return a boolean
   */
  default boolean isSet() {

    throw new UnsupportedOperationException();
  }

  /**
   * <p>forkJoin.</p>
   *
   * @param pool a {@link java.util.concurrent.ForkJoinPool} object
   * @return a T object
   */
  T forkJoin(ForkJoinPool pool);

  /**
   * <p>andThen.</p>
   *
   * @param consumer a {@link java.util.function.Consumer} object
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default Lazy<T> andThen(Consumer<T> consumer) {

    return of(() -> {
      T t = get();
      consumer.accept(t);
      return t;
    });
  }

  /**
   * <p>forkJoin.</p>
   *
   * @return a T object
   */
  default T forkJoin() {

    return forkJoin(ForkJoinPool.commonPool());
  }

  /**
   * <p>ifSet.</p>
   *
   * @param consumer a {@link java.util.function.Consumer} object
   */
  default void ifSet(Consumer<T> consumer) {

    if (isSet()) {
      consumer.accept(get());
    }
  }

  /**
   * <p>map.</p>
   *
   * @param function a {@link java.util.function.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default <E> Lazy<E> map(Function<T, E> function) {

    return of(this, function);
  }

  /**
   * <p>set.</p>
   *
   * @param value a T object
   * @return a T object
   */
  default T set(T value) {

    throw new UnsupportedOperationException();
  }

  /**
   * <p>wrap.</p>
   *
   * @param operator a {@link java.util.function.UnaryOperator} object
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default Lazy<T> wrap(UnaryOperator<Supplier<T>> operator) {

    return of(operator.apply(this));
  }

  class UnsetException extends RuntimeException {
  }

}
