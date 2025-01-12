package com.trigram.zero.flow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.function.UnaryOperator;

/**
 * <p>IOChain interface.</p>
 *
 * @author wolray
 */
public interface IOChain<T> {

  /**
   * <p>of.</p>
   *
   * @param t a T object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  static <T> IOChain<T> of(T t) {

    return () -> t;
  }

  /**
   * <p>of.</p>
   *
   * @param runnable a {@link com.trigram.zero.flow.IOChain.Runnable} object
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  static IOChain<Void> of(Runnable runnable) {

    return () -> {
      runnable.run();
      return null;
    };
  }

  /**
   * <p>of.</p>
   *
   * @param supplier a {@link com.trigram.zero.flow.IOChain} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  static <T> IOChain<T> of(IOChain<T> supplier) {

    return supplier;
  }

  /**
   * <p>ofReader.</p>
   *
   * @param supplier a {@link com.trigram.zero.flow.IOChain} object
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  static IOChain<BufferedReader> ofReader(IOChain<Reader> supplier) {

    return (Closable<BufferedReader>) () -> {
      Reader reader = supplier.call();
      return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    };
  }

  /**
   * <p>ofWriter.</p>
   *
   * @param supplier a {@link com.trigram.zero.flow.IOChain} object
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  static IOChain<BufferedWriter> ofWriter(IOChain<Writer> supplier) {

    return (Closable<BufferedWriter>) () -> {
      Writer writer = supplier.call();
      return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    };
  }

  /**
   * <p>apply.</p>
   *
   * @param t a T object
   * @param function a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param <T> a T class
   * @param <E> a E class
   * @return a E object
   */
  static <T, E> E apply(T t, Function<T, E> function) {

    try {
      return function.apply(t);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * <p>call.</p>
   *
   * @return a T object
   * @throws java.io.IOException if any.
   */
  T call() throws IOException;

  /**
   * <p>apply.</p>
   *
   * @param function a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param <E> a E class
   * @return a E object
   */
  default <E> E apply(Function<T, E> function) {

    try {
      return function.apply(call());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * <p>asLazy.</p>
   *
   * @return a {@link com.trigram.zero.flow.Lazy} object
   */
  default Lazy<T> asLazy() {

    return Lazy.of(this::get);
  }

  /**
   * <p>get.</p>
   *
   * @return a T object
   */
  default T get() {

    try {
      return call();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * <p>map.</p>
   *
   * @param function a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  default <E> IOChain<E> map(Function<T, E> function) {

    return () -> function.apply(call());
  }

  /**
   * <p>mapClosable.</p>
   *
   * @param function a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param <C> a C class
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  default <C extends Closeable> IOChain<C> mapClosable(Function<T, C> function) {

    return (Closable<C>) () -> function.apply(call());
  }

  /**
   * <p>peek.</p>
   *
   * @param consumer a {@link com.trigram.zero.flow.IOChain.Consumer} object
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  default IOChain<T> peek(Consumer<T> consumer) {

    return () -> {
      T t = call();
      consumer.accept(t);
      return t;
    };
  }

  /**
   * <p>toSeq.</p>
   *
   * @param provider a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> toSeq(Function<T, E> provider) {

    return c -> use(t -> {
      E e;
      while ((e = provider.apply(t)) != null) {
        c.accept(e);
      }
    });
  }

  /**
   * <p>use.</p>
   *
   * @param consumer a {@link com.trigram.zero.flow.IOChain.Consumer} object
   */
  default void use(Consumer<T> consumer) {

    useAndGet(consumer);
  }

  /**
   * <p>useAndGet.</p>
   *
   * @param consumer a {@link com.trigram.zero.flow.IOChain.Consumer} object
   * @return a T object
   */
  default T useAndGet(Consumer<T> consumer) {

    try {
      T t = call();
      consumer.accept(t);
      return t;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * <p>toSeq.</p>
   *
   * @param provider a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param n a int
   * @param replace a {@link java.util.function.UnaryOperator} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> toSeq(Function<T, E> provider, int n, UnaryOperator<E> replace) {

    return c -> use(t -> {
      E e;
      for (int i = 0; i < n; i++) {
        e = provider.apply(t);
        if (e == null) {
          return;
        }
        c.accept(replace.apply(e));
      }
      while ((e = provider.apply(t)) != null) {
        c.accept(e);
      }
    });
  }

  /**
   * <p>toSeq.</p>
   *
   * @param provider a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param skip a int
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> toSeq(Function<T, E> provider, int skip) {

    return c -> use(t -> {
      E e;
      for (int i = 0; i < skip; i++) {
        e = provider.apply(t);
        if (e == null) {
          return;
        }
      }
      while ((e = provider.apply(t)) != null) {
        c.accept(e);
      }
    });
  }

  /**
   * <p>toSeq.</p>
   *
   * @param limit a long
   * @param provider a {@link com.trigram.zero.flow.IOChain.Function} object
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <E> ZeroFlow<E> toSeq(long limit, Function<T, E> provider) {

    return c -> use(t -> {
      for (long i = 0; i < limit; i++) {
        c.accept(provider.apply(t));
      }
    });
  }

  interface Closable<C extends Closeable> extends IOChain<C> {

    @Override
    default void use(Consumer<C> consumer) {

      try (C closable = call()) {
        consumer.accept(closable);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

  }

  interface Consumer<T> {

    void accept(T t) throws IOException;

  }

  interface Function<T, E> {

    E apply(T t) throws IOException;

  }

  interface Runnable {

    void run() throws IOException;

  }

}
