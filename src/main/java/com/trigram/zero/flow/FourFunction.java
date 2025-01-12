package com.trigram.zero.flow;

/**
 * <p>FourFunction interface.</p>
 *
 * @author wolray
 */
public interface FourFunction<A, B, C, D, T> {

  /**
   * <p>apply.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   * @param d a D object
   * @return a T object
   */
  T apply(A a, B b, C c, D d);

}
