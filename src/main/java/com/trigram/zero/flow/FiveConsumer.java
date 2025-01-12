package com.trigram.zero.flow;

/**
 * <p>FiveConsumer interface.</p>
 *
 * @author wolray
 */
public interface FiveConsumer<A, B, C, D, E> {

  /**
   * <p>accept.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   * @param d a D object
   * @param e a E object
   */
  void accept(A a, B b, C c, D d, E e);

}
