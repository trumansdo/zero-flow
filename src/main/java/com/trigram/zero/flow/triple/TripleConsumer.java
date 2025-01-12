package com.trigram.zero.flow.triple;

/**
 * @author wolray
 */
public interface TripleConsumer<A, B, C> {

  void accept(A a, B b, C c);

}
