package zjtech.websocket.termination.actuate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zjtech.websocket.termination.core.ISessionHolder;
import zjtech.websocket.termination.core.SessionHandler;

@Slf4j
@RestControllerEndpoint(id = "websocketOperation")
public class WebSocketMessageEndPoint {

  private final ISessionHolder sessionHolder;

  public WebSocketMessageEndPoint(ISessionHolder sessionHolder) {
    this.sessionHolder = sessionHolder;
  }

  /**
   * Send a text message to client.
   *
   * @param sessionId WebSocketSession ID
   * @param message text message
   * @return ResponseEntity
   */
  @PostMapping("/{sessionId}")
  public ResponseEntity sendMessageToClient(
      @PathVariable("sessionId") String sessionId, @RequestParam String message) {
    SessionHandler sessionHandler = sessionHolder.getSessionHandler(sessionId);
    if (sessionHandler == null) {
      log.warn("Failed to send message to client by sessionId '{}'", sessionId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(String.format("The session '%s' doesn't exist.", sessionId));
    }

    sessionHandler.sendText(message);
    log.debug(
        "sent a message to client by sessionId '{}', and the message is '{}'", sessionId, message);
    return ResponseEntity.ok().build();
  }

  /**
   * Remove a WebSocketSession by session id.
   *
   * @param sessionId WebSocketSession ID
   * @return ResponseEntity
   */
  @DeleteMapping("/{sessionId}")
  public ResponseEntity closeSession(@PathVariable("sessionId") String sessionId) {
    SessionHandler sessionHandler = sessionHolder.remove(sessionId);
    if (sessionHandler == null) {
      log.warn("Failed to close a session by sessionId '{}'", sessionId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(String.format("The session '%s' doesn't exist.", sessionId));
    }
    sessionHandler.close();
    log.debug("Closed a session by sessionId '{}'", sessionId);
    return ResponseEntity.ok().build();
  }
}
