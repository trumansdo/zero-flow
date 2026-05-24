# Zero-Flow 开发指南

## 项目概述

Zero-Flow 是一个**生成器风格 (Generator Style)** 的流式处理 API for Java，提供流畅的函数式编程数据流处理能力。

**Group ID:** io.github.trumansdo  
**Artifact ID:** zero-flow  
**包名:** com.trigram.zero.flow

---

## 设计理念

### 1. 生成器模式 (Generator Pattern)

核心思想：**数据流即消费者回调**。不是像 Java Stream 那样被动消费数据，而是主动"生成"数据供消费者使用。

```java
// ZeroFlow 本质上是一个函数：Consumer<T> -> void
public interface ZeroFlow<T> extends BaseZeroFlow<Consumer<T>> {
    void consume(Consumer<T> consumer);
}
```

这种设计让数据流可以：
- **延迟执行** - 数据在 `consume()` 调用时才流动
- **无限流** - 可以生成无限数据（如 `gen()`, `repeat()`）
- **可中断** - 通过 `StopException` 类似 return 的方式中断

### 2. 流程控制：StopException

使用异常作为流程控制机制（类似 Python 的 `return` 或 Kotlin 的 `return@`）：

```java
// 查找第一个符合条件的元素并中断流
Optional<T> find(Predicate<T> predicate) {
    Mutable<T> m = new Mutable<>(null);
    consumeTillStop(t -> {
        if (predicate.test(t)) {
            m.set(t);
            stop();  // 抛出 StopException 中断流
        }
    });
    return m.toOptional();
}

// 使用 stop() 方法
static <T> T stop() {
    throw StopException.INSTANCE;
}
```

### 3. 延迟求值 (Lazy Evaluation)

所有转换操作（`map`, `filter`, `flatMap` 等）都是**延迟的**，不立即执行：

```java
// 这不会执行任何操作，只是创建一个新的 ZeroFlow
ZeroFlow<Integer> mapped = ZeroFlow.of(1, 2, 3).map(x -> x * 2);

// 只有调用 consume 时才真正执行
mapped.consume(System.out::println);
```

### 4. 链式调用 (Fluent API)

返回 `ZeroFlow<E>` 以支持链式调用：

```java
ZeroFlow.of(1, 2, 3, 4, 5)
    .filter(x -> x > 2)
    .map(x -> x * 2)
    .distinct()
    .consume(System.out::println);  // 输出: 6, 8, 10
```

### 5. Reducer 对标 Java Stream Collector

`Reducer<T, V>` 对标 `java.util.stream.Collector`，提供更灵活的数据聚合能力：

```java
// Collector: supplier + accumulator + finisher
// Reducer:   supplier + accumulator + finisher
public interface Reducer<T, V> {
    Supplier<V> supplier();           // 创建容器
    BiConsumer<V, T> accumulator();   // 累加元素
    Consumer<V> finisher();           // 最终转换
}
```

### 6. Transducer 组合 map-reduce

`Transducer` 将 map 和 reduce 操作组合，支持高效的流式处理：

```java
// 等价于: stream.map(f).reduce(identity, accumulator)
Transducer<T, V, E> = Reducer + Transformer
```

---

## 重要接口

### BaseZeroFlow<C>

所有数据流的基接口：

```java
public interface BaseZeroFlow<C> {
    void consume(C consumer);
    
    // 可中断的消费（捕获 StopException）
    default void consumeTillStop(C consumer) {
        try {
            consume(consumer);
        } catch (StopException ignore) { }
    }
}
```

### ZeroFlow<T>

核心数据流接口，继承 `BaseZeroFlow<Consumer<T>>`，提供：
- 流的构造 (`of()`, `gen()`, `repeat()`, `empty()`)
- 中间操作 (`map()`, `filter()`, `flatMap()`, `distinct()`)
- 终端操作 (`reduce()`, `fold()`, `collect()`, `first()`, `last()`)

### IntZeroFlow

原始 `int` 类型的特化，避免装箱/拆箱开销。提供 `range()`, `of(int...)` 等工厂方法。

### Reducer<T, V>

数据聚合容器，等价于 Java Stream 的 `Collector`，但更灵活。

### Transducer<T, V, E>

组合 map-reduce 操作的转换器，结合 `Reducer` 和 `Transformer`。

### PairZeroFlow<K, V>

二元流，消费接口为 `BiConsumer<K, V>`，对应 Map 的 `forEach`。

### TripleZeroFlow<A, B, C>

三元流，消费接口为 `TripleConsumer<A, B, C>`。

### Lazy<T>

延迟求值容器，扩展 `Supplier<T>`，支持懒加载和缓存。

---

## 使用示例

### 1. 基本创建和消费

```java
// 从数组创建
ZeroFlow<Integer> flow = ZeroFlow.of(1, 2, 3, 4, 5);

// 从集合创建
List<String> list = Arrays.asList("a", "b", "c");
ZeroFlow<String> flow2 = ZeroFlow.of(list);

// 无限生成器
ZeroFlow<Integer> infinite = ZeroFlow.gen(0, x -> x + 1);  // 0, 1, 2, 3, ...

// 有限生成器
ZeroFlow<Integer> fibonacci = ZeroFlow.gen(1, 1, Integer::sum).take(10);  // 1, 1, 2, 3, 5, 8, ...

// 重复元素
ZeroFlow<String> repeated = ZeroFlow.repeat(3, "hello");  // hello, hello, hello

// 范围
IntZeroFlow range = IntZeroFlow.range(0, 10, 2);  // 0, 2, 4, 6, 8
```

### 2. 转换操作

```java
// map - 转换每个元素
ZeroFlow.of(1, 2, 3)
    .map(x -> x * 2)  // 2, 4, 6
    .consume(System.out::println);

// filter - 过滤元素
ZeroFlow.of(1, 2, 3, 4, 5)
    .filter(x -> x % 2 == 0)  // 2, 4
    .consume(System.out::println);

// flatMap - 展平嵌套流
ZeroFlow.of(Arrays.asList(1, 2), Arrays.asList(3, 4))
    .flatMap(ZeroFlow::of)  // 1, 2, 3, 4
    .consume(System.out::println);

// mapNotNull - 过滤 null
ZeroFlow.of(1, null, 2, null, 3)
    .mapNotNull(x -> x * 2)  // 2, 4, 6
    .consume(System.out::println);

// distinct - 去重
ZeroFlow.of(1, 2, 2, 3, 3, 3)
    .distinct()  // 1, 2, 3
    .consume(System.out::println);
```

### 3. 聚合操作

```java
// reduce - 归约
int sum = ZeroFlow.of(1, 2, 3, 4, 5)
    .reduce(Integer::sum);  // 15

// fold - 折叠（类似 reduce 但有初始值）
String joined = ZeroFlow.of("a", "b", "c")
    .fold(new StringJoiner(","), (j, s) -> j.add(s))
    .toString();  // "a,b,c"

// collect - 收集到集合
List<Integer> list = ZeroFlow.of(1, 2, 3).toList();
Set<Integer> set = ZeroFlow.of(1, 2, 3).reduce(Reducer.toSet());
Map<String, Integer> map = ZeroFlow.of(1, 2, 3)
    .reduce(Reducer.toMap(x -> "key" + x, x -> x));

// groupBy - 分组
MapZeroFlow<Integer, ListZeroFlow<Integer>> grouped = ZeroFlow.of(1, 2, 3, 4, 5)
    .groupBy(x -> x % 2);  // {0: [2, 4], 1: [1, 3, 5]}

// partition - 分区
Pair<List<Integer>, List<Integer>> partitioned = ZeroFlow.of(1, 2, 3, 4, 5)
    .reduce(Reducer.partition(x -> x % 2 == 0));
// first: [2, 4], second: [1, 3, 5]

// count, sum, average
int count = ZeroFlow.of(1, 2, 3).count();
double avg = ZeroFlow.of(1, 2, 3).average(x -> x);
```

### 4. 取元素操作

```java
// first/last - 第一个/最后一个元素
Integer first = ZeroFlow.of(1, 2, 3).first();  // 1
Integer last = ZeroFlow.of(1, 2, 3).last();    // 3

// find - 查找符合条件的第一个
Optional<Integer> found = ZeroFlow.of(1, 2, 3, 4, 5)
    .find(x -> x > 3);  // Optional.of(4)

// take/drop - 取/丢弃前 n 个
ZeroFlow.of(1, 2, 3, 4, 5).take(3);   // 1, 2, 3
ZeroFlow.of(1, 2, 3, 4, 5).drop(2);   // 3, 4, 5

// takeWhile/dropWhile - 条件取/丢弃
ZeroFlow.of(1, 2, 3, 4, 5)
    .takeWhile(x -> x < 3);  // 1, 2
ZeroFlow.of(1, 2, 3, 4, 5)
    .dropWhile(x -> x < 3);  // 3, 4, 5
```

### 5. 高级操作

```java
// chunked - 分块
ZeroFlow.of(1, 2, 3, 4, 5, 6)
    .chunked(2)  // [1,2], [3,4], [5,6]
    .map(list -> list.join(","))  // "1,2", "3,4", "5,6"
    .join("|")  // "1,2|3,4|5,6"

// mapSub - 子序列提取（类似字符串子串）
ZeroFlow.of(1, 2, 3, 4, 5)
    .mapSub(x -> x < 3, Reducer.toList())  // [1,2], [3,4,5]

// pair - 配对操作
ZeroFlow.of(1, 2, 3, 4)
    .mapPair(false)  // [1,2], [3,4]
    .mapPair(true)   // [1,2], [2,3], [3,4]

// mapPair 带条件的
ZeroFlow.of(1, 1, 2, 2, 3)
    .takeWhileEquals()  // 1, 1
    .drop(1).takeWhileEquals(i -> i / 2)  // 1, 2

// duplicate - 复制
ZeroFlow.of(1, 2, 3)
    .duplicateEach(2)  // 1, 1, 2, 2, 3, 3
    .duplicateAll(2)   // 1, 2, 3, 1, 2, 3

// circle - 循环
ZeroFlow.of(1, 2, 3)
    .circle()          // 无限循环 1, 2, 3, 1, 2, 3, ...
    .take(7)           // 1, 2, 3, 1, 2, 3, 1
```

### 6. 树结构遍历

```java
// 二叉树遍历
Node root = new Node(0, 
    new Node(1, new Node(3), new Node(4)),
    new Node(2, new Node(5), null)
);

// 深度优先遍历
ZeroFlow.ofTree(root, n -> ZeroFlow.of(n.left, n.right))
    .map(n -> n.value)  // 0, 1, 3, 4, 2, 5
    .consume(System.out::println);
```

### 7. 字符串处理

```java
// 正则匹配
String text = "hello(world)foo(bar)baz";
ZeroFlow.match(text, Pattern.compile("\\((\\w+)\\)"))
    .map(m -> m.group(1))  // world, bar
    .join(",");  // "world,bar"

// 字符流
IntZeroFlow.of("hello")
    .mapToObj(i -> (char) i)  // 'h','e','l','l','o'
    .consume(System.out::println);

// 子串提取
IntZeroFlow.of("(ab)c(d)e(f)")
    .mapToObj(i -> (char) i)
    .mapSub('(', ')')  // [a,b], [d], [f]
    .map(ls -> ls.join(""))
    .join(",");  // "ab,d,f"
```

### 8. 并行处理

```java
// 并行处理
ZeroFlow.of(1, 2, 3, 4, 5)
    .parallel()  // 使用公共线程池并行处理
    .map(x -> {
        Thread.sleep(100);
        return x * 2;
    })
    .toList();
```

### 9. 使用 Reducer 进行复杂聚合

```java
// 使用 Reducer 进行多步聚合
List<Student> students = Arrays.asList(
    new Student("一年级", "1班", "张三"),
    new Student("一年级", "1班", "李四"),
    new Student("二年级", "1班", "王五")
);

// 多级分组
MapZeroFlow<String, MapZeroFlow<String, ListZeroFlow<Student>>> grouped = ZeroFlow.of(students)
    .groupBy(
        s -> s.grade,
        Reducer.groupBy(
            s -> s.className,
            Reducer.toList()
        )
    );
```

---

## 构建命令

### 构建项目

```bash
# 清理并构建（默认跳过测试）
mvn clean install

# 构建不运行测试
mvn clean install -DskipTests

# 构建并运行测试
mvn clean install -DskipTests=false
```

### 运行测试

```bash
# 运行所有测试（必须先启用测试）
mvn test -DskipTests=false

# 运行单个测试类
mvn test -DskipTests=false -Dtest=SeqTest
```

---

## 常见问题

1. **测试不运行:** 记住添加 `-DskipTests=false`
2. **测试包名:** 测试在 `com.trigram.wolray.zero.flow` 而不是 `com.trigram.zero.flow`
3. **流不执行:** 检查是否调用了 `consume()` 或其他终端操作
