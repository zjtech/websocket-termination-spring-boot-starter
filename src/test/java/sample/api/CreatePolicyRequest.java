package sample.api;

import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.Request;
import zjtech.websocket.termination.api.WebSocketCommand;

@Getter
@Setter
@WebSocketCommand("CREATE_POLICY")
public class CreatePolicyRequest implements Request {

  private String name;
  private String description;
}
