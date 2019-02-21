package zjtech.websocket.termination.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import zjtech.websocket.termination.common.WsUtils;

/** Spring WebFlux websocket support does not yet support STOMP nor message brokers */
@Slf4j
public class DefaultWebSocketHandler implements WebSocketHandler {

  private final WsUtils utils;

  public DefaultWebSocketHandler(WsUtils utils) {
    this.utils = utils;
  }

  @Override
  public Mono<Void> handle(WebSocketSession webSocketSession) {
    // handle the websocket session
    SessionHandler sessionHandler = utils.getBean(SessionHandler.class);
    Mono<Void> sessionMono = sessionHandler.handle(webSocketSession);

    // handle the received message
    MessageHandler messageHandler = utils.getBean(MessageHandler.class);
    Mono<Void> messageMono = messageHandler.handle(sessionHandler);

    // PING message handler
    PingHandler pingHandler = utils.getBean(PingHandler.class);
    pingHandler.asyncHandle(sessionHandler);

    return Mono.zip(sessionMono, messageMono)
        .doOnError(thr -> log.warn("unexpected exception.", thr))
        .then();
  }
}
