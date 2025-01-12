package com.trigram.zero.flow.pair;

/**
 * 以{@link java.lang.Integer}为left，{@link java.lang.Double}为right的Pair
 *
 * @author wolray
 */
public class IntDoublePair extends Pair<Integer, Double> {

  public int first;

  public double second;

  /**
   * <p>Constructor for IntDoublePair.</p>
   *
   * @param first a int
   * @param second a double
   */
  public IntDoublePair(int first, double second) {

    super(first, second);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return String.format("(%d,%f)", first, second);
  }

}
