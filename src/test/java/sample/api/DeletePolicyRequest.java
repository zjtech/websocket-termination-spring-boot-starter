package sample.api;

import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.BaseRequest;
import zjtech.websocket.termination.api.WebSocketCommand;

@Getter
@Setter
@WebSocketCommand("DELETE_POLICY")
public class DeletePolicyRequest extends BaseRequest {

  private String id;
}
