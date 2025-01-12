package com.trigram.zero.flow;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Abstract AsyncZeroFlow class.</p>
 *
 * @author wolray
 */
public abstract class AsyncZeroFlow<T> implements ZeroFlow<T> {

  protected final Async async;

  protected final ZeroFlow<T> source;

  protected Object task;

  protected boolean cancelled;

  AsyncZeroFlow(Async async, ZeroFlow<T> source) {

    this.async  = async;
    this.source = source;
  }

  /**
   * <p>cancel.</p>
   */
  public void cancel() {

    cancelled = true;
    joinConsume();
  }

  /**
   * <p>joinConsume.</p>
   */
  public void joinConsume() {

    if (task != null) {
      async.join(task);
    }
  }

  /**
   * <p>checkState.</p>
   */
  protected void checkState() {

    if (task != null) {
      throw new IllegalStateException("AsyncSeq can only consume once");
    }
  }

  /**
   * <p>onStart.</p>
   *
   * @param runnable a {@link java.lang.Runnable} object
   * @return a {@link com.trigram.zero.flow.AsyncZeroFlow} object
   */
  public AsyncZeroFlow<T> onStart(Runnable runnable) {

    return new AsyncZeroFlow<T>(async, source) {

      @Override
      public void consume(Consumer<T> consumer) {

        runnable.run();
        AsyncZeroFlow.this.consume(consumer);
      }
    };
  }

  /**
   * <p>onCompletion.</p>
   *
   * @param runnable a {@link java.lang.Runnable} object
   * @return a {@link com.trigram.zero.flow.AsyncZeroFlow} object
   */
  public AsyncZeroFlow<T> onCompletion(Runnable runnable) {

    return new AsyncZeroFlow<T>(async, source) {

      @Override
      public void consume(Consumer<T> consumer) {

        AsyncZeroFlow.this.consume(consumer);
        runnable.run();
      }
    };
  }

  /** {@inheritDoc} */
  @Override
  public <E> AsyncZeroFlow<E> map(Function<T, E> function) {

    return new AsyncZeroFlow<E>(async, source.map(function)) {

      @Override
      public void consume(Consumer<E> consumer) {

        AsyncZeroFlow.this.consume(t -> consumer.accept(function.apply(t)));
      }
    };
  }

}
