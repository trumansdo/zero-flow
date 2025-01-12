package com.trigram.zero.flow.triple;

/**
 * <p>TripleFunction interface.</p>
 *
 * @author wolray
 */
public interface TripleFunction<A, B, C, T> {

  /**
   * <p>apply.</p>
   *
   * @param a a A object
   * @param b a B object
   * @param c a C object
   * @return a T object
   */
  T apply(A a, B b, C c);

}
