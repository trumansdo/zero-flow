package com.trigram.zero.flow;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 既是流，也是{@link java.util.LinkedHashMap}
 *
 * @author wolray
 */
public class LinkedHashMapZeroFlow<K, V> extends LinkedHashMap<K, V> implements MapZeroFlow<K, V> {

  /**
   * <p>Constructor for LinkedHashMapZeroFlow.</p>
   *
   * @param initialCapacity a int
   */
  public LinkedHashMapZeroFlow(int initialCapacity) {

    super(initialCapacity);
  }

  /**
   * <p>Constructor for LinkedHashMapZeroFlow.</p>
   */
  public LinkedHashMapZeroFlow() {

  }

  /**
   * <p>Constructor for LinkedHashMapZeroFlow.</p>
   *
   * @param m a {@link java.util.Map} object
   */
  public LinkedHashMapZeroFlow(Map<? extends K, ? extends V> m) {

    super(m);
  }

  /** {@inheritDoc} */
  @Override
  public SetZeroFlow<K> keysSeq() {

    return SetZeroFlow.of(keySet());
  }

  /** {@inheritDoc} */
  @Override
  public CollectionZeroFlow<V> valuesSeq() {

    return CollectionZeroFlow.of(values());
  }

  /** {@inheritDoc} */
  @Override
  public <A, B> MapZeroFlow<A, B> newForMapping() {

    return new LinkedHashMapZeroFlow<>(size());
  }

  /** {@inheritDoc} */
  @Override
  public SetZeroFlow<Entry<K, V>> entrySeq() {

    return SetZeroFlow.of(entrySet());
  }

}
