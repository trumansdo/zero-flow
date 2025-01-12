package com.trigram.zero.flow.pair;

/**
 * 以{@link java.lang.Integer}为left，{@link T}为right的Pair
 *
 * @author wolray
 */
public class IntPair<T> extends Pair<Integer, T> {

  /**
   * <p>Constructor for IntPair.</p>
   *
   * @param first a int
   * @param second a T object
   */
  public IntPair(int first, T second) {

    super(first, second);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return String.format("(%d,%s)", first, second);
  }

}
