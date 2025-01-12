package com.trigram.zero.flow;

import java.util.Collection;
import java.util.LinkedList;

/**
 * 既是流，也是{@link java.util.LinkedList}
 *
 * @author wolray
 */
public class LinkedListZeroFlow<T> extends LinkedList<T> implements ListZeroFlow<T>, QueueZeroFlow<T> {

  /**
   * <p>Constructor for LinkedListZeroFlow.</p>
   */
  public LinkedListZeroFlow() {

  }

  /**
   * <p>Constructor for LinkedListZeroFlow.</p>
   *
   * @param c a {@link java.util.Collection} object
   */
  public LinkedListZeroFlow(Collection<? extends T> c) {

    super(c);
  }

}
