package zjtech.websocket.termination.core;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.Request;

@Getter
@Setter
public class ConsumerContext<T extends Request> implements IConsumerContext {

  private SessionHandler sessionHandler;
  private String command;
  private Map<String, ?> headers;
  private T payload;
}
