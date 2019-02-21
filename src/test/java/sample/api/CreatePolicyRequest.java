package sample.api;

import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.BaseRequest;
import zjtech.websocket.termination.api.WebSocketCommand;

@Getter
@Setter
@WebSocketCommand("CREATE_POLICY")
public class CreatePolicyRequest extends BaseRequest {

  private String name;
  private String description;
}
