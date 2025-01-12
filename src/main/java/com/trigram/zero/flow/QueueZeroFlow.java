package com.trigram.zero.flow;

import java.util.Queue;

/**
 * 快速将{@link java.util.Queue}转成流处理
 *
 * @author wolray
 */
public interface QueueZeroFlow<T> extends SizedZeroFlow<T>, Queue<T> {

  /**
   * <p>of.</p>
   *
   * @param queue a {@link java.util.Queue} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.QueueZeroFlow} object
   */
  static <T> QueueZeroFlow<T> of(Queue<T> queue) {

    return queue instanceof QueueZeroFlow ? (QueueZeroFlow<T>) queue : new Proxy<>(queue);
  }

  class Proxy<T> extends CollectionZeroFlow.Proxy<T, Queue<T>> implements QueueZeroFlow<T> {

    public Proxy(Queue<T> backer) {

      super(backer);
    }

    @Override
    public boolean offer(T t) {

      return backer.offer(t);
    }

    @Override
    public T remove() {

      return backer.remove();
    }

    @Override
    public T poll() {

      return backer.poll();
    }

    @Override
    public T element() {

      return backer.element();
    }

    @Override
    public T peek() {

      return backer.peek();
    }

  }

}
