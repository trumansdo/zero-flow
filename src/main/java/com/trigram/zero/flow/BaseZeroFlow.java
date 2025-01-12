package com.trigram.zero.flow;

/**
 * 所有流的Base接口
 *
 * @param <C>
 *     消费回调函数
 * @author wolray
 */
public interface BaseZeroFlow<C> {

  /**
   * 消费，也是生产，也是通道
   *
   * @param consumer a C object
   */
  void consume(C consumer);

  /**
   * 可中断的
   *
   * @param consumer a C object
   */
  default void consumeTillStop(C consumer) {

    try {
      consume(consumer);
    } catch (StopException ignore) {
    }
  }

}
