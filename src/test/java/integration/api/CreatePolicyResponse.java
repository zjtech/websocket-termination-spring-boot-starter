package integration.api;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import zjtech.websocket.termination.api.BaseResponse;

@Getter
@Setter
public class CreatePolicyResponse extends BaseResponse {

  private int errorCode = 200;
  private String errorMessage;
  private String command;

  private Payload payload = new Payload();

  @Getter
  @Setter
  public static class Payload {

    private String id;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private String creater;
    private boolean validPolicy;
  }
}
