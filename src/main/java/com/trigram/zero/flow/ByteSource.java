package com.trigram.zero.flow;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.UnaryOperator;

/**
 * <p>ByteSource interface.</p>
 *
 * @author wolray
 */
public interface ByteSource extends IOChain.Closable<InputStream> {

  /**
   * <p>of.</p>
   *
   * @param file a {@link java.io.File} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource of(File file) {

    return of(file.toPath());
  }

  /**
   * <p>of.</p>
   *
   * @param path a {@link java.nio.file.Path} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource of(Path path) {

    return new ByteSource() {

      @Override
      public InputStream call() throws IOException {

        return Files.newInputStream(path);
      }

      @Override
      public byte[] toBytes() {

        return IOChain.apply(path, Files::readAllBytes);
      }

      @Override
      public IOChain<BufferedReader> toReader() {

        return (Closable<BufferedReader>) () -> Files.newBufferedReader(path, charset());
      }

      @Override
      public Path write(Path target) {

        if (!path.equals(target)) {
          IOChain.apply(path, p -> Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING));
        }
        return target;
      }
    };
  }

  /**
   * <p>of.</p>
   *
   * @param is a {@link java.io.InputStream} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource of(InputStream is) {

    return () -> is;
  }

  /**
   * <p>of.</p>
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource of(Iterable<String> iterable) {

    return of(iterable, "\n");
  }

  /**
   * <p>of.</p>
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @param separator a {@link java.lang.String} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource of(Iterable<String> iterable, String separator) {

    return () -> ItrUtil.toInputStream(iterable.iterator(), separator);
  }

  /**
   * <p>ofArray.</p>
   *
   * @param bytes a {@link com.trigram.zero.flow.IOChain} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource ofArray(IOChain<byte[]> bytes) {

    return of(bytes.get());
  }

  /**
   * <p>of.</p>
   *
   * @param bytes an array of {@link byte} objects
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource of(byte[] bytes) {

    return new ByteSource() {

      @Override
      public InputStream call() {

        return new ByteArrayInputStream(bytes);
      }

      @Override
      public byte[] toBytes() {

        return bytes;
      }

      @Override
      public ByteSource cache() {

        return this;
      }
    };
  }

  /**
   * <p>ofPath.</p>
   *
   * @param path a {@link com.trigram.zero.flow.IOChain} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource ofPath(IOChain<Path> path) {

    return of(path.get());
  }

  /**
   * <p>ofResource.</p>
   *
   * @param resource a {@link java.lang.String} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource ofResource(String resource) {

    return ofResource(ByteSource.class, resource);
  }

  /**
   * <p>ofResource.</p>
   *
   * @param cls a {@link java.lang.Class} object
   * @param resource a {@link java.lang.String} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource ofResource(Class<?> cls, String resource) {

    return IOChain.apply(cls.getResource(resource), url -> {
      try {
        if (url == null) {
          throw new FileNotFoundException(resource);
        }
        return of(Paths.get(url.toURI()));
      } catch (URISyntaxException | FileSystemNotFoundException e) {
        return of(url);
      }
    });
  }

  /**
   * <p>of.</p>
   *
   * @param url a {@link java.net.URL} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource of(URL url) {

    return url::openStream;
  }

  /**
   * <p>ofStream.</p>
   *
   * @param is a {@link com.trigram.zero.flow.IOChain} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource ofStream(IOChain<InputStream> is) {

    return is::call;
  }

  /**
   * <p>ofUrl.</p>
   *
   * @param url a {@link java.lang.String} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  static ByteSource ofUrl(String url) {

    return IOChain.apply(url, u -> of(new URL(u)));
  }

  /**
   * <p>charset.</p>
   *
   * @return a {@link java.nio.charset.Charset} object
   */
  default Charset charset() {

    return Charset.defaultCharset();
  }

  /**
   * <p>asString.</p>
   *
   * @return a {@link java.lang.String} object
   */
  default String asString() {

    return new String(toBytes(), charset());
  }

  /**
   * <p>toBytes.</p>
   *
   * @return an array of {@link byte} objects
   */
  default byte[] toBytes() {

    return toBytes(8192);
  }

  /**
   * <p>toBytes.</p>
   *
   * @param bufferSize a int
   * @return an array of {@link byte} objects
   */
  default byte[] toBytes(int bufferSize) {

    byte[]       buff  = new byte[bufferSize];
    List<byte[]> list  = new ArrayList<>();
    int[]        total = {0};
    use(is -> {
      int len;
      while ((len = is.read(buff, 0, buff.length)) > 0) {
        list.add(Arrays.copyOf(buff, len));
        total[0] += len;
      }
    });
    byte[] res = new byte[total[0]];
    int    pos = 0;
    for (byte[] bytes : list) {
      System.arraycopy(bytes, 0, res, pos, bytes.length);
      pos += bytes.length;
    }
    return res;
  }

  /**
   * <p>cache.</p>
   *
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  default ByteSource cache() {

    return of(toBytes());
  }

  /**
   * <p>toProperties.</p>
   *
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  default IOChain<Properties> toProperties() {

    return toReader().map(r -> {
      Properties p = new Properties();
      p.load(r);
      return p;
    });
  }

  /**
   * <p>toReader.</p>
   *
   * @return a {@link com.trigram.zero.flow.IOChain} object
   */
  default IOChain<BufferedReader> toReader() {

    return mapClosable(is -> new BufferedReader(new InputStreamReader(is, charset())));
  }

  /**
   * <p>toSeq.</p>
   *
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<String> toSeq() {

    return toReader().toSeq(BufferedReader::readLine);
  }

  /**
   * <p>toSeq.</p>
   *
   * @param n a int
   * @param replace a {@link java.util.function.UnaryOperator} object
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<String> toSeq(int n, UnaryOperator<String> replace) {

    return toReader().toSeq(BufferedReader::readLine, n, replace);
  }

  /**
   * <p>toSeq.</p>
   *
   * @param skip a int
   * @return a {@link com.trigram.zero.flow.ZeroFlow} object
   */
  default ZeroFlow<String> toSeq(int skip) {

    return toReader().toSeq(BufferedReader::readLine, skip);
  }

  /**
   * <p>withCharset.</p>
   *
   * @param charset a {@link java.nio.charset.Charset} object
   * @return a {@link com.trigram.zero.flow.ByteSource} object
   */
  default ByteSource withCharset(Charset charset) {

    ByteSource origin = this;
    return new ByteSource() {

      @Override
      public InputStream call() throws IOException {

        return origin.call();
      }

      @Override
      public Charset charset() {

        return charset;
      }

      @Override
      public byte[] toBytes() {

        return origin.toBytes();
      }

      @Override
      public Path write(Path target) {

        return origin.write(target);
      }
    };
  }

  /**
   * <p>write.</p>
   *
   * @param target a {@link java.nio.file.Path} object
   * @return a {@link java.nio.file.Path} object
   */
  default Path write(Path target) {

    use(is -> Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING));
    return target;
  }

  /**
   * <p>writeTemp.</p>
   *
   * @param suffix a {@link java.lang.String} object
   * @return a {@link java.nio.file.Path} object
   */
  default Path writeTemp(String suffix) {

    return IOChain.of(() -> write(Files.createTempFile("", suffix))).get();
  }

}
