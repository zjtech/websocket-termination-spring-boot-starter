package zjtech.websocket.termination.common;

import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.BaseRequest;

@Getter
@Setter
public class RequestWrapper<T extends BaseRequest> implements BaseRequest {

  private String command;
  private T request;
}
