package com.trigram.zero.flow.pair;

/**
 * 以{@link java.lang.Double}为left，{@link T}为right的Pair
 *
 * @author wolray
 */
public class DoublePair<T> extends Pair<Double, T> {

  /**
   * <p>Constructor for DoublePair.</p>
   *
   * @param first a double
   * @param second a T object
   */
  public DoublePair(double first, T second) {

    super(first, second);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return String.format("(%f,%s)", first, second);
  }

}
