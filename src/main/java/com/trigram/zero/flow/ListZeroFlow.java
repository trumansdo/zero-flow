package com.trigram.zero.flow;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * 快速将{@link java.util.List}转成流处理
 *
 * @author wolray
 */
public interface ListZeroFlow<T> extends SizedZeroFlow<T>, List<T> {

  /**
   * <p>of.</p>
   *
   * @param list a {@link java.util.List} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.ListZeroFlow} object
   */
  static <T> ListZeroFlow<T> of(List<T> list) {

    return list instanceof ListZeroFlow ? (ListZeroFlow<T>) list : new Proxy<>(list);
  }

  class Proxy<T> extends CollectionZeroFlow.Proxy<T, List<T>> implements ListZeroFlow<T> {

    public Proxy(List<T> backer) {

      super(backer);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {

      return backer.addAll(c);
    }

    @Override
    public T get(int index) {

      return backer.get(index);
    }

    @Override
    public T set(int index, T element) {

      return backer.set(index, element);
    }

    @Override
    public void add(int index, T element) {

      backer.add(index, element);
    }

    @Override
    public T remove(int index) {

      return backer.remove(index);
    }

    @Override
    public int indexOf(Object o) {

      return backer.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {

      return backer.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {

      return backer.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {

      return backer.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {

      return backer.subList(fromIndex, toIndex);
    }

  }

}
