package zjtech.websocket.termination.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import zjtech.websocket.termination.common.WsUtils;

@Slf4j
public class DefaultWebSocketHandler implements WebSocketHandler {

  private final WsUtils utils;

  public DefaultWebSocketHandler(WsUtils utils) {
    this.utils = utils;
  }

  @Override
  public Mono<Void> handle(WebSocketSession webSocketSession) {
    Mono<Void> sessionMono;
    Mono<Void> messageMono;
    try {
      // handle the websocket session
      SessionHandler sessionHandler = utils.getBean(SessionHandler.class);
      sessionMono = sessionHandler.handle(webSocketSession);

      // handle the received message
      MessageHandler messageHandler = utils.getBean(MessageHandler.class);
      messageMono = messageHandler.handle(sessionHandler);

      // PING/PONG message handler
      PingPongHandler pingPongHandler = utils.getBean(PingPongHandler.class);
      pingPongHandler.asyncHandle(sessionHandler);
    } catch (Exception e) {
      log.error("Failed to handle the websocket session", e);
      return Mono.empty();
    }

    return Mono.zip(sessionMono, messageMono).then();
  }
}
