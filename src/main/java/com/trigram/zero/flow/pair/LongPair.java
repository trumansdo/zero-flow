package com.trigram.zero.flow.pair;

/**
 * 以{@link java.lang.Long}为left，{@link T}为right的Pair
 *
 * @author wolray
 */
public class LongPair<T> extends Pair<Long, T> {

  /**
   * <p>Constructor for LongPair.</p>
   *
   * @param first a long
   * @param second a T object
   */
  public LongPair(long first, T second) {

    super(first, second);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return String.format("(%d,%s)", first, second);
  }

}
