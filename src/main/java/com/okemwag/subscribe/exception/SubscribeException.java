package com.okemwag.subscribe.exception;

public class SubscribeException extends RuntimeException {
  public SubscribeException(String message) {
    super(message);
  }

  public SubscribeException(String message, Throwable cause) {
    super(message, cause);
  }
}
