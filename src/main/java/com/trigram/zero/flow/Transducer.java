package com.trigram.zero.flow;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * reduce操作时的转换器，对应 {@link java.util.stream.Collector#finisher()} 方法
 *
 * @param <T>
 *     原值
 * @param <V>
 *     如果transformer不存在，就是最后的转换值
 * @param <E>
 *     如果transformer存在将V转成E
 * @author wolray
 */
public interface Transducer<T, V, E> {

  /**
   * <p>of.</p>
   *
   * @param collector a {@link java.util.stream.Collector} object
   * @param <T> a T class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T, V, E> Transducer<T, V, E> of(Collector<T, V, E> collector) {

    return of(Reducer.of(collector.supplier(), collector.accumulator()), collector.finisher());
  }

  /**
   * <p>of.</p>
   *
   * @param reducer a {@link com.trigram.zero.flow.Reducer} object
   * @param transformer a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T, V, E> Transducer<T, V, E> of(Reducer<T, V> reducer, Function<V, E> transformer) {

    return new Transducer<T, V, E>() {

      @Override
      public Reducer<T, V> reducer() {

        return reducer;
      }

      @Override
      public Function<V, E> transformer() {

        return transformer;
      }
    };
  }

  /**
   * 不变换类型的reduce操作，比如数值的计算
   *
   * @param binaryOperator a {@link java.util.function.BinaryOperator} object
   * @param <T> a T class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T> Transducer<T, ?, T> of(BinaryOperator<T> binaryOperator) {

    return of(() -> new Mutable<T>(null), (m, t) -> {
      if (m.isSet) {
        m.it = binaryOperator.apply(m.it, t);
      } else {
        m.set(t);
      }
    }, Mutable::get);
  }

  /**
   * <p>of.</p>
   *
   * @param supplier a {@link java.util.function.Supplier} object
   * @param accumulator a {@link java.util.function.BiConsumer} object
   * @param transformer a {@link java.util.function.Function} object
   * @param <T> a T class
   * @param <V> a V class
   * @param <E> a E class
   * @return a {@link com.trigram.zero.flow.Transducer} object
   */
  static <T, V, E> Transducer<T, V, E> of(
      Supplier<V> supplier, BiConsumer<V, T> accumulator, Function<V, E> transformer
  ) {

    return of(Reducer.of(supplier, accumulator), transformer);
  }

  /**
   * map/reduce理论中的reduce收束/归约/归纳器
   *
   * @return a {@link com.trigram.zero.flow.Reducer} object
   */
  Reducer<T, V> reducer();

  /**
   * 对应{@link java.util.stream.Collector#finisher()} 方法。 <br/> 将reduce的最终结果再进行一次转换
   *
   * @return a {@link java.util.function.Function} object
   */
  Function<V, E> transformer();

}
