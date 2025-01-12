package com.trigram.zero.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * 既是流，也是{@link java.util.ArrayList}
 *
 * @author wolray
 */
public class ArrayListZeroFlow<T> extends ArrayList<T> implements ListZeroFlow<T> {

  /**
   * <p>Constructor for ArrayListZeroFlow.</p>
   *
   * @param initialCapacity a int
   */
  public ArrayListZeroFlow(int initialCapacity) {

    super(initialCapacity);
  }

  /**
   * <p>Constructor for ArrayListZeroFlow.</p>
   */
  public ArrayListZeroFlow() {

  }

  /**
   * <p>Constructor for ArrayListZeroFlow.</p>
   *
   * @param c a {@link java.util.Collection} object
   */
  public ArrayListZeroFlow(Collection<? extends T> c) {

    super(c);
  }

  /**
   * <p>swap.</p>
   *
   * @param i a int
   * @param j a int
   */
  public void swap(int i, int j) {

    T t = get(i);
    set(i, get(j));
    set(j, t);
  }

  /**
   * <p>permute.</p>
   *
   * @param inplace a boolean
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  public ZeroFlow<ArrayListZeroFlow<T>> permute(boolean inplace) {

    return c -> permute(c, inplace, 0);
  }

  private void permute(Consumer<ArrayListZeroFlow<T>> c, boolean inplace, int i) {
    // 感觉是给快速排序用的
    int n = size();
    if (i == n) {
      c.accept(inplace ? this : new ArrayListZeroFlow<>(this));
      return;
    }
    for (int j = i; j < n; j++) {
      swap(i, j);
      permute(c, inplace, i + 1);
      swap(i, j);
    }
  }


}

