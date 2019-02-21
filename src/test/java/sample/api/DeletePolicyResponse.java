package sample.api;

import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.BaseResponse;

@Getter
@Setter
public class DeletePolicyResponse extends BaseResponse {
  private int errorCode = 200;
  private String errorMessage;
  private String command;
}
