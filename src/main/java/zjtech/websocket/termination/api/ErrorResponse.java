package zjtech.websocket.termination.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse extends Response {
  private int errorCode = 200;
  private String errorMessage;
  private String command;
}
