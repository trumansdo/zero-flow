package com.trigram.zero.flow;

/**
 * <p>SixFunction interface.</p>
 *
 * @author wolray
 */
public interface SixFunction<A, B, C, D, E, F, T> {

  /**
   * <p>apply.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   * @param d a D object
   * @param e a E object
   * @param f a F object
   * @return a T object
   */
  T apply(A a, B b, C c, D d, E e, F f);

}
