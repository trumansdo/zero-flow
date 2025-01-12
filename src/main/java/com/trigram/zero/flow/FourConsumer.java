package com.trigram.zero.flow;

/**
 * <p>FourConsumer interface.</p>
 *
 * @author wolray
 */
public interface FourConsumer<A, B, C, D> {

  /**
   * <p>accept.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   * @param d a D object
   */
  void accept(A a, B b, C c, D d);

}
