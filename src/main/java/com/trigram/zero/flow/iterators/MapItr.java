package com.trigram.zero.flow.iterators;

import java.util.Iterator;

/**
 * 可在获取每个数据时对数据进行map/reduce操作里面的map转换操作的迭代器
 *
 * @author wolray
 */
public abstract class MapItr<T, E> implements Iterator<E> {

  private final Iterator<T> iterator;

  /**
   * <p>Constructor for MapItr.</p>
   *
   * @param iterator a {@link java.util.Iterator} object
   */
  public MapItr(Iterator<T> iterator) {

    this.iterator = iterator;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasNext() {

    return iterator.hasNext();
  }

  /** {@inheritDoc} */
  @Override
  public E next() {

    return apply(iterator.next());
  }

  /**
   * 在迭代过程中对每个数据进行转换操作
   *
   * @return {@link E}
   * @param t a T object
   */
  public abstract E apply(T t);

}
