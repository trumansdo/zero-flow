package com.trigram.zero.flow.pair;

/**
 * 以{@link java.lang.Boolean}为left，{@link T}为right的Pair
 *
 * @author wolray
 */
public class BooleanPair<T> extends Pair<Boolean, T> {

  public boolean first;

  public T second;

  /**
   * <p>Constructor for BooleanPair.</p>
   *
   * @param first a boolean
   * @param second a T object
   */
  public BooleanPair(boolean first, T second) {

    super(first, second);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return String.format("(%b,%s)", first, second);
  }

}
