package zjtech.websocket.termination.api;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketMessage<T> {

  @NotBlank private String command;
  private Map<String, String> headers = new HashMap<>();
  private T payload;
}
