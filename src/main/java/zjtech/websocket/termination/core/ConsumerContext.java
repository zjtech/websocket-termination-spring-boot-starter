package zjtech.websocket.termination.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zjtech.websocket.termination.api.BaseRequest;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerContext implements IConsumerContext {

  private SessionHandler sessionHandler;

  private BaseRequest request;

  private String command;
}
