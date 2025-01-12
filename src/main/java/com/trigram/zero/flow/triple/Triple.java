package com.trigram.zero.flow.triple;

/**
 * <p>Triple class.</p>
 *
 * @author wolray
 */
public class Triple<T, A, B> {

  public T first;

  public A second;

  public B third;

  /**
   * <p>Constructor for Triple.</p>
   *
   * @param first a T object
   * @param second a A object
   * @param third a B object
   */
  public Triple(T first, A second, B third) {

    this.first  = first;
    this.second = second;
    this.third  = third;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return String.format("(%s,%s,%s)", first, second, third);
  }

}
