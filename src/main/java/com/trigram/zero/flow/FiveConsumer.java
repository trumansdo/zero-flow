package com.trigram.zero.flow;

/**
 * @author wolray
 */
public interface FiveConsumer<A, B, C, D, E> {

  void accept(A a, B b, C c, D d, E e);

}
