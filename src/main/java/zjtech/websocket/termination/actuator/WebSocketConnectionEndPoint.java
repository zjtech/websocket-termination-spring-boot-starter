package zjtech.websocket.termination.actuator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import zjtech.websocket.termination.core.CommandMappingHandler;
import zjtech.websocket.termination.core.ISessionHolder;

@Slf4j
@RestControllerEndpoint(id = "websocketInfo")
public class WebSocketConnectionEndPoint {

  private final ISessionHolder sessionHolder;

  private final CommandMappingHandler mappingHandler;

  public WebSocketConnectionEndPoint(
      ISessionHolder sessionHolder, CommandMappingHandler mappingHandler) {
    this.sessionHolder = sessionHolder;
    this.mappingHandler = mappingHandler;
  }

  @GetMapping
  public Map<String, Object> getStats() {
    List<String> sessionList = sessionHolder.getSessionList();
    Map<String, Object> map = new HashMap<>();
    map.put("activeSessionCount", sessionList.size());
    map.put("activeSessions", sessionHolder.getSessionList());
    map.put("mapping", mappingHandler.getMapingInfo());
    return map;
  }
}
