package com.trigram.wolray.zero.flow;

import com.trigram.zero.flow.BatchedZeroFlow;
import com.trigram.zero.flow.IntZeroFlow;
import com.trigram.zero.flow.ListZeroFlow;
import com.trigram.zero.flow.MapZeroFlow;
import com.trigram.zero.flow.Reducer;
import com.trigram.zero.flow.ZeroFlow;
import com.trigram.zero.flow.pair.Pair;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FundAnalysis {

  static class Holding {
    final String code, name, sector;
    final double weight;
    Holding(String code, String name, double weight, String sector) {
      this.code = code; this.name = name; this.weight = weight; this.sector = sector;
    }
  }

  static class Snapshot {
    final String code;
    final double price, changePct;
    Snapshot(String code, double price, double changePct) {
      this.code = code; this.price = price; this.changePct = changePct;
    }
  }

  static class Enriched {
    final Holding h;
    final Snapshot s;
    Enriched(Holding h, Snapshot s) { this.h = h; this.s = s; }
    double marketValue() { return s.price * h.weight / 100; }
  }

  static List<Holding> loadHoldings() {
    return Arrays.asList(
        new Holding("600519", "贵州茅台", 8.5, "消费"),
        new Holding("000858", "五粮液", 5.2, "消费"),
        new Holding("300750", "宁德时代", 7.8, "新能源"),
        new Holding("601318", "中国平安", 4.6, "金融"),
        new Holding("600036", "招商银行", 3.9, "金融"),
        new Holding("000333", "美的集团", 3.2, "家电"),
        new Holding("002415", "海康威视", 2.8, "科技"),
        new Holding("300059", "东方财富", 2.5, "金融"),
        new Holding("600887", "伊利股份", 2.3, "消费"),
        new Holding("000651", "格力电器", 2.1, "家电"),
        new Holding("002714", "牧原股份", 1.8, "农业"),
        new Holding("601012", "隆基绿能", 1.7, "新能源"),
        new Holding("600309", "万华化学", 1.5, "化工"),
        new Holding("002475", "立讯精密", 1.4, "科技"),
        new Holding("300124", "汇川技术", 1.3, "新能源")
    );
  }

  static Map<String, Snapshot> fetchTodayPrices() {
    return ZeroFlow.of(
        new Snapshot("600519", 1523.50, -0.35),
        new Snapshot("000858", 134.20, 1.25),
        new Snapshot("300750", 198.60, 2.18),
        new Snapshot("601318", 42.35, -0.82),
        new Snapshot("600036", 34.78, 0.45),
        new Snapshot("000333", 62.50, 1.05),
        new Snapshot("002415", 32.15, -1.20),
        new Snapshot("300059", 14.28, 2.55),
        new Snapshot("600887", 25.60, 0.30),
        new Snapshot("000651", 38.90, -0.60),
        new Snapshot("002714", 42.30, -1.85),
        new Snapshot("601012", 16.85, 3.20),
        new Snapshot("600309", 78.40, 0.95),
        new Snapshot("002475", 32.80, 1.45),
        new Snapshot("300124", 62.30, -0.55)
    ).toMap(s -> s.code, s -> s);
  }

  public static void main(String[] args) {
    List<Holding> holdings = loadHoldings();
    Map<String, Snapshot> prices = fetchTodayPrices();
    System.out.println("====== " + LocalDate.now() + " 基金持仓分析 ======\n");

    // 1. sortWith + filter + map + println
    System.out.println("--- 持仓权重 > 3% 的重仓股 ---");
    ZeroFlow.of(holdings)
        .filter(h -> h.weight > 3)
        .sortWith((a, b) -> Double.compare(b.weight, a.weight))
        .map(h -> String.format("  %s(%s): %.1f%%", h.name, h.code, h.weight))
        .println();

    // 2. enrich + map + sortWith
    System.out.println("\n--- 持仓市值排行 ---");
    ZeroFlow.of(holdings)
        .filter(h -> prices.containsKey(h.code))
        .map(h -> new Enriched(h, prices.get(h.code)))
        .sortWith((a, b) -> Double.compare(b.marketValue(), a.marketValue()))
        .map(e -> String.format("  %s: %.0f元 (%.2f%%)", e.h.name, e.marketValue(), e.s.changePct))
        .println();

    // 3. fold 聚合
    System.out.println("\n--- 汇总 ---");
    double totalValue = ZeroFlow.of(holdings)
        .filter(h -> prices.containsKey(h.code))
        .map(h -> prices.get(h.code).price * h.weight / 100)
        .fold(0.0, (sum, v) -> sum + v);
    System.out.printf("  估算持仓市值: %.0f元%n", totalValue);

    double weightedReturn = ZeroFlow.of(holdings)
        .filter(h -> prices.containsKey(h.code))
        .map(h -> prices.get(h.code).changePct * h.weight)
        .fold(0.0, (sum, v) -> sum + v);
    System.out.printf("  组合加权涨跌: %.2f%%%n", weightedReturn);

    // 4. groupBy 行业分组
    System.out.println("\n--- 行业分布 ---");
    MapZeroFlow<String, ListZeroFlow<Holding>> bySector = ZeroFlow.of(holdings)
        .sortWith((a, b) -> Double.compare(b.weight, a.weight))
        .groupBy(h -> h.sector);
    bySector.consume((sector, list) -> {
      double sw = list.fold(0.0, (sum, h) -> sum + h.weight);
      System.out.printf("  %s: %.1f%%%n", sector, sw);
      list.map(h -> String.format("    %s %.1f%%", h.name, h.weight)).println();
    });

    // 5. 多级分组 (行业 + 涨跌方向)
    System.out.println("\n--- 行业 + 涨跌方向 ---");
    ZeroFlow.of(holdings)
        .filter(h -> prices.containsKey(h.code))
        .groupBy(h -> h.sector,
            Reducer.groupBy(h -> prices.get(h.code).changePct > 0 ? "上涨" : "下跌",
                Reducer.<Holding>toList()))
        .consume((sector, dirMap) -> {
          System.out.println("  " + sector + ":");
          dirMap.consume((dir, list) -> {
            System.out.printf("    %s (%d只)%n", dir, list.count());
            list.map(h -> String.format("      %s %.2f%%", h.name, prices.get(h.code).changePct)).println();
          });
        });

    // 6. partition 涨跌分区
    System.out.println("\n--- 涨跌分区 ---");
    Pair<BatchedZeroFlow<Holding>, BatchedZeroFlow<Holding>> split = ZeroFlow.of(holdings)
        .filter(h -> prices.containsKey(h.code))
        .reduce(Reducer.<Holding>partition(h -> prices.get(h.code).changePct >= 0));
    System.out.printf("  上涨: %d只%n", split.first.count());
    split.first
        .sortWith((a, b) -> Double.compare(prices.get(b.code).changePct, prices.get(a.code).changePct))
        .map(h -> String.format("    %s +%.2f%%", h.name, prices.get(h.code).changePct))
        .println();
    System.out.printf("  下跌: %d只%n", split.second.count());

    // 7. take top N
    System.out.println("\n--- 今日涨幅前3 ---");
    ZeroFlow.of(holdings)
        .filter(h -> prices.containsKey(h.code))
        .sortWith((a, b) -> Double.compare(prices.get(b.code).changePct, prices.get(a.code).changePct))
        .take(3)
        .map(h -> String.format("  %s: %+.2f%%", h.name, prices.get(h.code).changePct))
        .println();

    System.out.println("\n--- 今日跌幅前3 ---");
    ZeroFlow.of(holdings)
        .filter(h -> prices.containsKey(h.code))
        .sortWith((a, b) -> Double.compare(prices.get(a.code).changePct, prices.get(b.code).changePct))
        .take(3)
        .map(h -> String.format("  %s: %+.2f%%", h.name, prices.get(h.code).changePct))
        .println();

    // 8. fold 构建排名
    System.out.println("\n--- 权重排名 ---");
    String ranking = ZeroFlow.of(holdings)
        .sortWith((a, b) -> Double.compare(b.weight, a.weight))
        .fold(new StringBuilder(), (sb, h) -> sb.append(String.format("  %s: %.1f%%%n", h.name, h.weight)))
        .toString();
    System.out.println(ranking);

    // 9. Reducer 行业聚合
    System.out.println("\n--- 行业权重 Reducer 聚合 ---");
    MapZeroFlow<String, ListZeroFlow<Holding>> grouped = ZeroFlow.of(holdings).groupBy(h -> h.sector);
    grouped.consume((sector, list) -> {
      double totalWeight = list.fold(0.0, (s, h) -> s + h.weight);
      System.out.printf("  %s: %.1f%% (%d只)%n", sector, totalWeight, list.count());
    });

    // 10. onEach 流式输出
    System.out.println("\n--- onEach 调试输出 ---");
    ZeroFlow.of(holdings)
        .sortWith((a, b) -> Double.compare(b.weight, a.weight))
        .onEach(h -> System.out.printf("  处理: %s(%.1f%%)%n", h.name, h.weight))
        .map(h -> h.name)
        .join(", ");
    System.out.println("  (onEach 是中间操作, 需要终端操作触发)");
  }
}
