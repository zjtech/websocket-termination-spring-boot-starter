package zjtech.websocket.termination.common;

public enum WsErrorCode {
  INVALID_JSON_DATA,
  COMMAND_REQUIRED,
  UNKNOWN_COMMAND,

  CLIENT_CLOSED,
  INTERNAL_ERROR;
}
