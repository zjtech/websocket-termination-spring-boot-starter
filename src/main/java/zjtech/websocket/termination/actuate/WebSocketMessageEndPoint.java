package zjtech.websocket.termination.actuate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;
import zjtech.websocket.termination.core.ISessionHolder;
import zjtech.websocket.termination.core.SessionHandler;

@Component
@WebEndpoint(id = "websocketMessage")
public class WebSocketMessageEndPoint {

  private final ISessionHolder sessionHolder;

  @Autowired
  public WebSocketMessageEndPoint(ISessionHolder sessionHolder) {
    this.sessionHolder = sessionHolder;
  }

  @WriteOperation
  public String sendMessageToClient(@Selector String sessionId, String message) {
    SessionHandler sessionHandler = sessionHolder.getSessionHandler(sessionId);
    if (sessionHandler == null) {
      return String.format("The session '%s' doesn't exist.", sessionId);
    }

    sessionHandler.sendText(message);
    return "the message is sent successfully.";
  }

  @DeleteOperation
  public String deleteSession(@Selector String sessionId) {
    SessionHandler sessionHandler = sessionHolder.remove(sessionId);
    if (sessionHandler == null) {
      return String.format("The session '%s' doesn't exist.", sessionId);
    }
    sessionHandler.close();
    return String.format("The session '%s' is closed ", sessionId);
  }
}
