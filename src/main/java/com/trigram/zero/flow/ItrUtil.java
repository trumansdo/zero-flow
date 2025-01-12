package com.trigram.zero.flow;

import com.trigram.zero.flow.iterators.MapItr;
import com.trigram.zero.flow.iterators.PickItr;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 迭代器工具，用于提供各种从原迭代器包装后的迭代器
 *
 * @author wolray
 */
public interface ItrUtil {

  /**
   * 包装一个删除前面n个数据的迭代器
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param n a int
   * @param <T> a T class
   * @return a {@link java.util.Iterator} object
   */
  static <T> Iterator<T> drop(Iterator<T> iterator, int n) {

    return n <= 0 ? iterator : new PickItr<T>() {

      int i = n;

      @Override
      public T pick() {

        for (; i > 0; i--) {
          pop(iterator);
        }
        return pop(iterator);
      }
    };
  }

  /**
   * <p>pop.</p>
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param <T> a T class
   * @return a T object
   */
  static <T> T pop(Iterator<T> iterator) {

    return iterator.hasNext() ? iterator.next() : ZeroFlow.stop();
  }

  /**
   * 包装一个按条件删除的迭代器
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param predicate a {@link java.util.function.Predicate} object
   * @param <T> a T class
   * @return a {@link java.util.Iterator} object
   */
  static <T> Iterator<T> dropWhile(Iterator<T> iterator, Predicate<T> predicate) {

    return new PickItr<T>() {

      boolean flag = true;

      @Override
      public T pick() {

        T t = pop(iterator);
        if (flag) {
          while (predicate.test(t)) {
            t = pop(iterator);
          }
          flag = false;
        }
        return t;
      }
    };
  }

  /**
   * 包装成按条件过滤的迭代器
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param predicate a {@link java.util.function.Predicate} object
   * @param <T> a T class
   * @return a {@link java.util.Iterator} object
   */
  static <T> Iterator<T> filter(Iterator<T> iterator, Predicate<T> predicate) {

    return new PickItr<T>() {

      @Override
      public T pick() {

        while (iterator.hasNext()) {
          T t = iterator.next();
          if (predicate.test(t)) {
            return t;
          }
        }
        return ZeroFlow.stop();
      }
    };
  }

  /**
   * 包装展开二维可迭代对象的迭代器
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.iterators.PickItr} object
   */
  static <T, E> PickItr<E> flat(Iterator<T> iterator, Function<T, ? extends Iterable<E>> function) {

    return flat(map(iterator, function));
  }

  /**
   * 展开二维可迭代对象的迭代器
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.iterators.PickItr} object
   */
  static <T> PickItr<T> flat(Iterator<? extends Iterable<T>> iterator) {

    return new PickItr<T>() {

      Iterator<T> cur = Collections.emptyIterator();

      @Override
      public T pick() {

        while (!cur.hasNext()) {
          cur = pop(iterator).iterator();
        }
        return cur.next();
      }
    };
  }

  /**
   * <p>map.</p>
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param function a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <E> a E class
   * @return a {@link java.util.Iterator} object
   */
  static <T, E> Iterator<E> map(Iterator<T> iterator, Function<T, E> function) {

    return new MapItr<T, E>(iterator) {

      @Override
      public E apply(T t) {

        return function.apply(t);
      }
    };
  }

  /**
   * 展开{@link java.util.Optional} 的迭代器
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.iterators.PickItr} object
   */
  static <T> PickItr<T> flatOptional(Iterator<Optional<T>> iterator) {

    return new PickItr<T>() {

      @Override
      public T pick() {

        while (iterator.hasNext()) {
          Optional<T> opt = iterator.next();
          if (opt.isPresent()) {
            return opt.get();
          }
        }
        return ZeroFlow.stop();
      }
    };
  }

  /**
   * 保持与方法{@link com.trigram.zero.flow.ZeroFlow#consume(Consumer, int, Consumer)} 判断逻辑，小于等于0用另一个逻辑替换处理
   *
   * @param function
   *     小于等于0的处理
   * @param substitute
   *     大于0的处理
   * @param iterator a {@link java.util.Iterator} object
   * @param n a int
   * @param <T> a T class
   * @param <E> a E class
   * @return a {@link java.util.Iterator} object
   */
  static <T, E> Iterator<E> map(Iterator<T> iterator, Function<T, E> function, int n, Function<T, E> substitute) {

    return new MapItr<T, E>(iterator) {

      int i = n - 1;

      @Override
      public E apply(T t) {

        if (i < 0) {
          return function.apply(t);
        } else {
          i--;
          return substitute.apply(t);
        }
      }
    };
  }

  /**
   * 包装可对索引和值转换的迭代器
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param function a {@link com.trigram.zero.flow.ZeroFlow.IndexObjFunction} object
   * @param <T> a T class
   * @param <E> a E class
   * @return a {@link java.util.Iterator} object
   */
  static <T, E> Iterator<E> mapIndexed(Iterator<T> iterator, ZeroFlow.IndexObjFunction<T, E> function) {

    return new MapItr<T, E>(iterator) {

      int i = 0;

      @Override
      public E apply(T t) {

        return function.apply(i++, t);
      }
    };
  }

  /**
   * 获取前面n个数据
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param n a int
   * @param <T> a T class
   * @return a {@link java.util.Iterator} object
   */
  static <T> Iterator<T> take(Iterator<T> iterator, int n) {

    return n <= 0 ? Collections.emptyIterator() : new PickItr<T>() {

      int i = n;

      @Override
      public T pick() {

        return i-- > 0 ? pop(iterator) : ZeroFlow.stop();
      }
    };
  }

  /**
   * 包装成按条件获取数据的迭代器
   *
   * @param function
   *     转换当前数据
   * @param testPrevCurr
   *     上一个和当前已转换后数据为参数的谓词
   * @param iterator a {@link java.util.Iterator} object
   * @param <T> a T class
   * @param <E> a E class
   * @return a {@link java.util.Iterator} object
   */
  static <T, E> Iterator<T> takeWhile(Iterator<T> iterator, Function<T, E> function, BiPredicate<E, E> testPrevCurr) {

    return new PickItr<T>() {

      E last = null;

      @Override
      public T pick() {

        T t    = pop(iterator);
        E curr = function.apply(t);
        if (last == null || testPrevCurr.test(last, curr)) {
          last = curr;
          return t;
        } else {
          return ZeroFlow.stop();
        }
      }
    };
  }

  /**
   * 包装成按条件获取数据的迭代器，条件不满足时停止
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param predicate a {@link java.util.function.Predicate} object
   * @param <T> a T class
   * @return a {@link java.util.Iterator} object
   */
  static <T> Iterator<T> takeWhile(Iterator<T> iterator, Predicate<T> predicate) {

    return new PickItr<T>() {

      @Override
      public T pick() {

        T t = pop(iterator);
        return predicate.test(t) ? t : ZeroFlow.stop();
      }
    };
  }

  /**
   * <p>toInputStream.</p>
   *
   * @return {@link java.io.InputStream}
   * @see #toInputStream(Iterator, String)
   * @param iterable a {@link java.lang.Iterable} object
   * @param separator a {@link java.lang.String} object
   */
  static InputStream toInputStream(Iterable<String> iterable, String separator) {

    return toInputStream(iterable.iterator(), separator);
  }

  /**
   * <p>toInputStream.</p>
   *
   * @return {@link java.io.InputStream}
   * @see InputStreamItr#InputStreamItr(Iterator, String)
   * @param iterator a {@link java.util.Iterator} object
   * @param separator a {@link java.lang.String} object
   */
  static InputStream toInputStream(Iterator<String> iterator, String separator) {

    return new InputStreamItr(iterator, separator);
  }

  /**
   * <p>toStream.</p>
   *
   * @param iterator a {@link java.util.Iterator} object
   * @param <T> a T class
   * @return a {@link java.util.stream.Stream} object
   */
  static <T> Stream<T> toStream(Iterator<T> iterator) {

    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
        false
    );
  }

  /**
   * 包装成往迭代器的每个数据中间插入一个指定数据
   *
   * @param t
   *     中间插入的指定数据
   * @param iterator a {@link java.util.Iterator} object
   * @param <T> a T class
   * @return a {@link java.util.Iterator} object
   */
  static <T> Iterator<T> zip(Iterator<T> iterator, T t) {

    return new PickItr<T>() {

      boolean flag = false;

      @Override
      public T pick() {
        // 通过这个交替返回插入原数据和分隔数据
        flag = !flag;
        return flag ? pop(iterator) : t;
      }
    };
  }

  /**
   * 迭代器流，将迭代转成IO流
   *
   * @version 1.0.0
   * @since 1.0.0
   */
  class InputStreamItr extends InputStream {

    final Iterator<byte[]> iterator;

    byte[] cur = {};

    int i;

    /**
     * @param itr
     *     原始迭代数据
     * @param sep
     *     在每个数据之间插入的数据
     */
    public InputStreamItr(Iterator<String> itr, String sep) {

      Iterator<byte[]> bytesIterator = map(itr, String::getBytes);
      iterator = sep.isEmpty() ? bytesIterator : zip(bytesIterator, sep.getBytes());
    }

    @Override
    public int read() {

      if (i < cur.length) {
        return cur[i++] & 0xFF;
      }
      i = 0;
      while (iterator.hasNext()) {
        cur = iterator.next();
        if (cur.length > 0) {
          return cur[i++] & 0xFF;
        }
      }
      return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) {

      int srcRest = cur.length - i;
      if (srcRest >= len) {
        System.arraycopy(cur, i, b, off, len);
        i += len;
        return len;
      } else {
        int count = 0;
        if (srcRest > 0) {
          System.arraycopy(cur, i, b, off, srcRest);
          off += srcRest;
          count += srcRest;
          i = cur.length;
        }
        while (count < len && iterator.hasNext()) {
          byte[] bytes = iterator.next();
          if (bytes.length > 0) {
            int desRest = len - count;
            if (bytes.length >= desRest) {
              System.arraycopy(cur = bytes, 0, b, off, desRest);
              i     = desRest;
              count = len;
            } else {
              System.arraycopy(bytes, 0, b, off, bytes.length);
              off += bytes.length;
              count += bytes.length;
            }
          }
        }
        return count > 0 ? count : -1;
      }
    }

  }

}
