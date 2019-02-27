package sample.api;

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

    private long id;
    private String name;
    private String description;
    private String createTime;
    private String creater;
    private boolean validPolicy;
  }
}
