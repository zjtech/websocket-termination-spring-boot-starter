package zjtech.websocket.termination.common;

public class WsConnectionException extends RuntimeException {

  private Throwable rawException;
  private String errorMessage;
  private WsErrorCode errorCode;

  public WsConnectionException() {}

  public WsConnectionException(WsErrorCode errorCode) {
    super(null, null, false, false);
    this.errorCode = errorCode;
  }

  public WsConnectionException(String message) {
    super(message, null, false, false);
    this.errorMessage = message;
  }

  public WsConnectionException(Throwable throwable) {
    super(null, throwable, false, false);
    this.rawException = throwable;
  }

  public WsConnectionException(String message, Throwable throwable) {
    super(message, throwable, false, false);
    this.rawException = throwable;
    this.errorMessage = message;
  }

  public Throwable getRawException() {
    return rawException;
  }

  public WsErrorCode getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
