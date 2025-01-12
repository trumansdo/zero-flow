package com.trigram.zero.flow;

/**
 * <p>FiveFunction interface.</p>
 *
 * @author wolray
 */
public interface FiveFunction<A, B, C, D, E, T> {

  /**
   * <p>apply.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   * @param d a D object
   * @param e a E object
   * @return a T object
   */
  T apply(A a, B b, C c, D d, E e);

}
