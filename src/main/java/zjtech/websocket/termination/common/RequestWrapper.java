package zjtech.websocket.termination.common;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.Request;

@Getter
@Setter
public class RequestWrapper<T extends Request> implements Request {

  private String command;
  private Map<String, ?> header = new HashMap<>();
  private T payload;
}
