package com.trigram.zero.flow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Splitter interface.</p>
 *
 * @author wolray
 */
public interface Splitter {

  /**
   * <p>of.</p>
   *
   * @param sep a {@link java.util.regex.Pattern} object
   * @return a {@link com.trigram.zero.flow.Splitter} object
   */
  static Splitter of(Pattern sep) {

    return (s, limit) -> c -> {
      char[]  chars   = s.toCharArray();
      Matcher matcher = sep.matcher(s);
      int     left    = limit, beg = 0;
      for (; left > 0 && matcher.find(); left--) {
        c.accept(substring(chars, beg, matcher.start()));
        beg = matcher.end();
      }
      if (left > 0) {
        c.accept(substring(chars, beg, chars.length));
      }
    };
  }

  /**
   * <p>substring.</p>
   *
   * @param chars an array of {@link char} objects
   * @param start a int
   * @param end a int
   * @return a {@link java.lang.String} object
   */
  static String substring(char[] chars, int start, int end) {

    return start < end ? new String(chars, start, end - start) : "";
  }

  /**
   * <p>of.</p>
   *
   * @param literal a {@link java.lang.String} object
   * @return a {@link com.trigram.zero.flow.Splitter} object
   */
  static Splitter of(String literal) {

    return literal.length() == 1 ? of(literal.charAt(0)) :
           literal.isEmpty() ? ofEmpty() : (s, limit) -> c -> {
             char[] chars = s.toCharArray();
             int    left  = limit, beg = 0, len = literal.length(), index;
             for (; left > 0 && (index = s.indexOf(literal, beg)) > 0; left--) {
               c.accept(substring(chars, beg, index));
               beg = index + len;
             }
             if (left > 0) {
               c.accept(substring(chars, beg, chars.length));
             }
           };
  }

  /**
   * <p>of.</p>
   *
   * @param sep a char
   * @return a {@link com.trigram.zero.flow.Splitter} object
   */
  static Splitter of(char sep) {

    return (s, limit) -> c -> {
      char[] chars = s.toCharArray();
      int    len   = chars.length, last = 0, left = limit;
      for (int i = 0; i < len && left > 0; i++) {
        if (chars[i] == sep) {
          c.accept(substring(chars, last, i));
          last = i + 1;
          left--;
        }
      }
      if (left > 0) {
        c.accept(substring(chars, last, len));
      }
    };
  }

  /**
   * <p>ofEmpty.</p>
   *
   * @return a {@link com.trigram.zero.flow.Splitter} object
   */
  static Splitter ofEmpty() {

    return (s, limit) -> ZeroFlow.unit(s);
  }

  /**
   * <p>split.</p>
   *
   * @param s a {@link java.lang.String} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<String> split(String s) {

    return split(s, Integer.MAX_VALUE);
  }

  /**
   * <p>split.</p>
   *
   * @param s a {@link java.lang.String} object
   * @param limit a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  ZeroFlow<String> split(String s, int limit);

}
