package com.trigram.zero.flow.triple;

/**
 * <p>TripleConsumer interface.</p>
 *
 * @author wolray
 */
public interface TripleConsumer<A, B, C> {

  /**
   * <p>accept.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   */
  void accept(A a, B b, C c);

}
