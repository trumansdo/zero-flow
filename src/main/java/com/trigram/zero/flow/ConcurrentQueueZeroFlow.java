package com.trigram.zero.flow;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>ConcurrentQueueZeroFlow class.</p>
 *
 * @author wolray
 */
public class ConcurrentQueueZeroFlow<T> extends ConcurrentLinkedQueue<T> implements QueueZeroFlow<T> {

  /**
   * <p>Constructor for ConcurrentQueueZeroFlow.</p>
   */
  public ConcurrentQueueZeroFlow() {

  }

  /**
   * <p>Constructor for ConcurrentQueueZeroFlow.</p>
   *
   * @param c a {@link java.util.Collection} object
   */
  public ConcurrentQueueZeroFlow(Collection<? extends T> c) {

    super(c);
  }

}
