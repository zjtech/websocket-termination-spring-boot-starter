package zjtech.websocket.termination.common;

public class WsConnectionException extends RuntimeException {

  private Throwable rawException;
  private WsErrorCode errorCode;

  public WsConnectionException(WsErrorCode errorCode) {
    super(null, null, false, false);
    this.errorCode = errorCode;
  }

  /**
   * Constructor.
   *
   * @param errorCode WsErrorCode
   * @param throwable Throwable
   */
  public WsConnectionException(WsErrorCode errorCode, Throwable throwable) {
    super(null, null, false, false);
    this.errorCode = errorCode;
    this.rawException = throwable;
  }

  public WsConnectionException(Throwable throwable) {
    super(null, throwable, false, false);
    this.rawException = throwable;
  }

  public Throwable getRawException() {
    return rawException;
  }

  public WsErrorCode getErrorCode() {
    return errorCode;
  }
}
