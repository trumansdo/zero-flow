package com.trigram.zero.flow.pair;

/**
 * <p>Pair class.</p>
 *
 * @author wolray
 */
public class Pair<A, B> {

  public A first;

  public B second;

  /**
   * <p>Constructor for Pair.</p>
   *
   * @param first a A object
   * @param second a B object
   */
  public Pair(A first, B second) {

    set(first, second);
  }

  /**
   * <p>set.</p>
   *
   * @param first a A object
   * @param second a B object
   */
  public void set(A first, B second) {

    this.first  = first;
    this.second = second;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return String.format("(%s,%s)", first, second);
  }

}
