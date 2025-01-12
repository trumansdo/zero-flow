package com.trigram.zero.flow;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * 既是流，也是{@link java.util.LinkedHashSet}
 *
 * @author wolray
 */
public class LinkedHashSetZeroFlow<T> extends LinkedHashSet<T> implements SetZeroFlow<T> {

  /**
   * <p>Constructor for LinkedHashSetZeroFlow.</p>
   *
   * @param initialCapacity a int
   */
  public LinkedHashSetZeroFlow(int initialCapacity) {

    super(initialCapacity);
  }

  /**
   * <p>Constructor for LinkedHashSetZeroFlow.</p>
   */
  public LinkedHashSetZeroFlow() {

  }

  /**
   * <p>Constructor for LinkedHashSetZeroFlow.</p>
   *
   * @param c a {@link java.util.Collection} object
   */
  public LinkedHashSetZeroFlow(Collection<? extends T> c) {

    super(c);
  }

}
