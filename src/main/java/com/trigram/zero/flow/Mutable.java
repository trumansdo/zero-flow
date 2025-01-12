package com.trigram.zero.flow;

import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

/**
 * 可变的数据对象存放容器
 *
 * @author wolray
 */
public class Mutable<T> implements Lazy<T> {

  protected boolean isSet = false;

  protected T it;

  /**
   * <p>Constructor for Mutable.</p>
   *
   * @param it a T object
   */
  public Mutable(T it) {

    this.it = it;
  }

  /** {@inheritDoc} */
  @Override
  public final T get() {

    if (isSet) {
      return it;
    }
    eval();
    isSet = true;
    return it;
  }

  /**
   * 计算字段{@link #it}的值
   */
  protected void eval() {

  }

  /** {@inheritDoc} */
  @Override
  public boolean isSet() {

    return isSet;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized final T forkJoin(ForkJoinPool pool) {

    if (isSet) {
      return it;
    }
    eval(pool);
    isSet = true;
    return it;
  }

  /**
   * <p>eval.</p>
   *
   * @param pool a {@link java.util.concurrent.ForkJoinPool} object
   */
  protected void eval(ForkJoinPool pool) {

    eval();
  }

  /** {@inheritDoc} */
  @Override
  public T set(T value) {

    isSet = true;
    return this.it = value;
  }

  /**
   * <p>toOptional.</p>
   *
   * @return a {@link java.util.Optional} object
   */
  public Optional<T> toOptional() {

    return isSet ? Optional.ofNullable(it) : Optional.empty();
  }

  /**
   * <p>Getter for the field <code>it</code>.</p>
   *
   * @return a T object
   */
  public T getIt() {

    return it;
  }

  /**
   * <p>Setter for the field <code>it</code>.</p>
   *
   * @param it a T object
   */
  public void setIt(T it) {

    this.it = it;
  }

}
