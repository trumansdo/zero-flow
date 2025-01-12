package com.trigram.zero.flow;

/**
 * <p>SixConsumer interface.</p>
 *
 * @author wolray
 */
public interface SixConsumer<A, B, C, D, E, F> {

  /**
   * <p>accept.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   * @param d a D object
   * @param e a E object
   * @param f a F object
   */
  void accept(A a, B b, C c, D d, E e, F f);

}
