package com.trigram.zero.flow;

import java.util.NoSuchElementException;

/**
 * <p>StopException class.</p>
 *
 * @author wolray
 */
public final class StopException extends NoSuchElementException {

  static final StopException INSTANCE = new StopException();

  private StopException() {

  }

  /** {@inheritDoc} */
  @Override
  public synchronized Throwable fillInStackTrace() {

    return this;
  }

}
