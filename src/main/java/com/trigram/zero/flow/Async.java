package com.trigram.zero.flow;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * <p>Async interface.</p>
 *
 * @author wolray
 */
public interface Async {

  /**
   * <p>common.</p>
   *
   * @return a {@link com.trigram.zero.flow.Async} object
   */
  static Async common() {

    return of(ForkJoinPool.commonPool());
  }

  /**
   * <p>of.</p>
   *
   * @param forkJoinPool a {@link java.util.concurrent.ForkJoinPool} object
   * @return a {@link com.trigram.zero.flow.Async} object
   */
  static Async of(ForkJoinPool forkJoinPool) {

    return new ForkJoin(forkJoinPool);
  }

  /**
   * <p>delay.</p>
   *
   * @param time a long
   */
  static void delay(long time) {

    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * <p>of.</p>
   *
   * @param executor a {@link java.util.concurrent.ExecutorService} object
   * @return a {@link com.trigram.zero.flow.Async} object
   */
  static Async of(ExecutorService executor) {

    return executor instanceof ForkJoinPool ? of((ForkJoinPool) executor) : new Async() {

      @Override
      public Object submit(Runnable runnable) {

        return CompletableFuture.runAsync(runnable, executor);
      }

      @Override
      public void join(Object task) {

        ((CompletableFuture<?>) task).join();
      }

      @Override
      public void joinAll(ZeroFlow<Runnable> tasks) {

        CompletableFuture<?>[] futures = tasks
            .map(t -> CompletableFuture.runAsync(t, executor))
            .toObjArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param factory a {@link java.util.concurrent.ThreadFactory} object
   * @return a {@link com.trigram.zero.flow.Async} object
   */
  static Async of(ThreadFactory factory) {

    return new Async() {

      @Override
      public Object submit(Runnable runnable) {

        Thread thread = factory.newThread(runnable);
        thread.start();
        return thread;
      }

      @Override
      public void join(Object task) {

        apply(((Thread) task)::join);
      }

      @Override
      public void joinAll(ZeroFlow<Runnable> tasks) {

        ListZeroFlow<Runnable> list = tasks.toList();
        CountDownLatch              latch = new CountDownLatch(list.size());
        list.consume(r -> factory.newThread(() -> {
          r.run();
          latch.countDown();
        }).start());
        apply(latch::wait);
      }
    };
  }

  /**
   * <p>apply.</p>
   *
   * @param runnable a {@link com.trigram.zero.flow.Async.ThreadRunnable} object
   */
  static void apply(ThreadRunnable runnable) {

    try {
      runnable.run();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * <p>sourceOf.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  static <T> ZeroFlow<T> sourceOf(ZeroFlow<T> seq) {

    return seq instanceof AsyncZeroFlow ? ((AsyncZeroFlow<T>) seq).source : seq;
  }

  /**
   * <p>join.</p>
   *
   * @param task a {@link java.lang.Object} object
   */
  void join(Object task);

  /**
   * <p>joinAll.</p>
   *
   * @param tasks a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  void joinAll(ZeroFlow<Runnable> tasks);

  /**
   * <p>toAsync.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.AsyncZeroFlow} object
   */
  default <T> AsyncZeroFlow<T> toAsync(ZeroFlow<T> seq) {

    return new AsyncZeroFlow<T>(this, sourceOf(seq)) {

      @Override
      public void consume(Consumer<T> consumer) {

        checkState();
        task = submit(() -> source.consumeTillStop(t -> {
          if (cancelled) {
            ZeroFlow.stop();
          }
          consumer.accept(t);
        }));
      }
    };
  }

  /**
   * <p>submit.</p>
   *
   * @param runnable a {@link java.lang.Runnable} object
   * @return a {@link java.lang.Object} object
   */
  Object submit(Runnable runnable);

  /**
   * <p>toChannel.</p>
   *
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.AsyncZeroFlow} object
   */
  default <T> AsyncZeroFlow<T> toChannel(ZeroFlow<T> seq) {

    return new AsyncZeroFlow<T>(this, sourceOf(seq)) {

      @Override
      public void consume(Consumer<T> consumer) {

        checkState();
        HotChannel<T> channel = new HotChannel<>();
        task = submit(() -> {
          source.consumeTillStop(t -> {
            if (cancelled) {
              ZeroFlow.stop();
            }
            if (channel.isEmpty()) {
              channel.offer(t);
              channel.easyNotify();
            } else {
              consumer.accept(t);
            }
          });
          channel.stop = true;
          channel.easyNotify();
        });
        while (true) {
          while (!channel.isEmpty()) {
            consumer.accept(channel.poll());
          }
          if (channel.stop) {
            break;
          }
          channel.easyWait();
        }
      }
    };
  }

  /**
   * <p>toShared.</p>
   *
   * @param buffer a int
   * @param delay a boolean
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <T> ZeroFlow<T> toShared(int buffer, boolean delay, ZeroFlow<T> seq) {

    ForkJoin.checkForHot(this);
    ZeroFlow<T>    source = sourceOf(seq);
    SharedArray<T> array  = new SharedArray<>(buffer);
    Runnable emit = () -> {
      source.consume(t -> {
        if (array.end < buffer) {
          array.add(t);
          array.end += 1;
        } else {
          array.set(array.head, t);
          if (++array.head == buffer) {
            array.head = 0;
          }
          array.drop += 1;
        }
        array.easyNotify();
      });
      array.stop = true;
      array.easyNotify();
    };
    AtomicReference<Object> task = new AtomicReference<>(null);
    if (!delay) {
      task.set(submit(emit));
    }
    return c -> {
      if (delay) {
        task.getAndUpdate(o -> o != null ? o : submit(emit));
      }
      submit(() -> {
        for (long i = array.drop; ; i++) {
          if (array.stop) {
            return;
          }
          if (i < array.drop) {
            c.accept(array.get(array.head));
            i = array.drop;
          } else {
            if (i - array.drop >= array.size()) {
              array.easyWait();
            }
            c.accept(array.get((int) ((array.head + i - array.drop) % buffer)));
          }
        }
      });
    };
  }

  /**
   * <p>toState.</p>
   *
   * @param delay a boolean
   * @param seq a {@link com.trigram.zero.flow.ZeroFlow} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default <T> ZeroFlow<T> toState(boolean delay, ZeroFlow<T> seq) {

    ForkJoin.checkForHot(this);
    ZeroFlow<T>   source = sourceOf(seq);
    StateValue<T> value  = new StateValue<>();
    Runnable emit = () -> {
      source.consume(t -> {
        if (!Objects.equals(t, value.it)) {
          value.it = t;
          value.easyNotify();
        }
      });
      value.stop = true;
      value.easyNotify();
    };
    AtomicReference<Object> task = new AtomicReference<>(null);
    if (!delay) {
      task.set(submit(emit));
    }
    return c -> {
      if (delay) {
        task.getAndUpdate(o -> o != null ? o : submit(emit));
      }
      submit(() -> {
        while (true) {
          value.easyWait();
          if (value.stop) {
            return;
          }
          c.accept(value.it);
        }
      });
    };
  }

  interface EasyLock {

    default void easyNotify() {

      synchronized (this) {
        notify();
      }
    }

    default void easyWait() {

      synchronized (this) {
        apply(this::wait);
      }
    }

  }

  interface ThreadRunnable {

    void run() throws InterruptedException;

  }

  class ForkJoin implements Async {

    final ForkJoinPool forkJoinPool;

    public ForkJoin(ForkJoinPool forkJoinPool) {

      this.forkJoinPool = forkJoinPool;
    }

    static void checkForHot(Async async) {

      if (async instanceof ForkJoin) {
        throw new IllegalArgumentException("do not use ForkJoinPool for shareIn or stateIn");
      }
    }

    @Override
    public Object submit(Runnable runnable) {

      return forkJoinPool.submit(runnable);
    }

    @Override
    public void join(Object task) {

      ((ForkJoinTask<?>) task).join();
    }

    @Override
    public void joinAll(ZeroFlow<Runnable> tasks) {

      tasks.map(forkJoinPool::submit).cache().consume(ForkJoinTask::join);
    }

  }

  class SharedArray<T> extends ArrayList<T> implements EasyLock {

    int head;

    int end;

    long drop;

    boolean stop;

    SharedArray(int buffer) {

      super(buffer);
    }

  }

  class StateValue<T> implements EasyLock {

    T it;

    boolean stop;

  }

}
