package zjtech.websocket.termination.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import zjtech.websocket.termination.api.ErrorResponse;
import zjtech.websocket.termination.common.WsErrorCode;
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
    SessionHandler sessionHandler = null;
    try {
      // handle the websocket session
      sessionHandler = utils.getBean(SessionHandler.class);
      sessionMono = sessionHandler.handle(webSocketSession);

      // handle the received message
      MessageHandler messageHandler = utils.getBean(MessageHandler.class);
      messageMono = messageHandler.handle(sessionHandler);

      // PING/PONG message handler
      PingPongHandler pingPongHandler = utils.getBean(PingPongHandler.class);
      pingPongHandler.asyncHandle(sessionHandler);
    } catch (Exception e) {
      log.error("Failed to handle the websocket session", e);
      if (sessionHandler != null) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(500);
        errorResponse.setErrorMessage(WsErrorCode.INTERNAL_ERROR.name());
        errorResponse.setCommand(WsErrorCode.INTERNAL_ERROR.name());
        sessionHandler.sendJsonString(errorResponse, webSocketSession);
      }

      return Mono.empty();
    }

    return Mono.zip(sessionMono, messageMono).then();
  }
}
