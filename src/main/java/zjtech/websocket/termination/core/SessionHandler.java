package zjtech.websocket.termination.core;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketMessage.Type;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.channel.AbortedException;
import zjtech.websocket.termination.common.WsUtils;
import zjtech.websocket.termination.config.WsConnectionConfigProps;

@Slf4j
public class SessionHandler {

  private final EmitterProcessor<SessionHandler> clientConnectedEventBus;
  private final EmitterProcessor<SessionHandler> clientDisconnectedEventBus;
  private DirectProcessor<String> messageProcessor;
  private DirectProcessor<WebSocketMessage> pongMessageProcessor;
  private WsUtils wsUtils;
  private WebSocketSession session;
  private String sessionId;
  private String clientInfo;
  private volatile boolean isConnected;
  private AtomicInteger pingCount = new AtomicInteger(0);
  private WsConnectionConfigProps configProps;

  public SessionHandler(
      WsUtils wsUtils,
      WsConnectionConfigProps configProps,
      EmitterProcessor<SessionHandler> clientConnectedEventBus,
      EmitterProcessor<SessionHandler> clientDisconnectedEventBus) {
    messageProcessor = DirectProcessor.create();
    this.pongMessageProcessor = DirectProcessor.create();
    this.clientConnectedEventBus = clientConnectedEventBus;
    this.clientDisconnectedEventBus = clientDisconnectedEventBus;
    this.wsUtils = wsUtils;
    this.configProps = configProps;
  }

  public Mono<Void> handle(WebSocketSession webSocketSession) {
    init(webSocketSession);

    // publish a session connected message
    notifyConnect();

    // handle received messages
    Mono<Void> receiveMono = handleReceive();
    return receiveMono.then();
  }

  private void init(WebSocketSession webSocketSession) {
    session = webSocketSession;
    sessionId = session.getId();
    clientInfo = sessionId + "," + session.getHandshakeInfo().getRemoteAddress().getHostString();
    isConnected = true;
  }

  private Mono<Void> handleReceive() {
    return session
        .receive()
        .takeUntil(val -> !this.isConnected)
        .subscribeOn(Schedulers.elastic())
        .doOnNext(this::publishTextMessage)
        .doOnComplete(
            () -> {
              log.info("The client '{}' is closed", clientInfo);
              messageProcessor.onComplete();
              notifyDisconnect();
            })
        .then();
  }

  /*
   * Receive a message in text format while the message type is not PING or PONG
   */
  private void publishTextMessage(WebSocketMessage msg) {
    if (msg.getType().equals(Type.PONG)) {
      pongMessageProcessor.onNext(msg);
      return;
    }
    String textMsg = msg.getPayloadAsText();
    if (!configProps.getPing().isSupressLog()) {
      log.info("Receive a message from client '{}' and the message is: {}", clientInfo, textMsg);
    }
    messageProcessor.onNext(textMsg);
  }

  public void sendJsonString(Object messageObj) {
    String textMessage = wsUtils.convertString(messageObj);
    sendText(textMessage);
  }

  public void sendText(String message) {
    this.send(session.textMessage(message));
  }

  public void send(WebSocketMessage msg) {
    if (isConnected) {
      session
          .send(Mono.just(msg))
          .doOnError(
              throwable -> {
                if (throwable instanceof ClosedChannelException
                    || throwable instanceof AbortedException) {
                  log.warn(
                      "Cannot sent message to client '{}' because it is disconnected.", clientInfo);
                  isConnected = false;
                }
              })
          .subscribe();
    } else {
      log.warn("Cannot sent message to client '{}' because it is disconnected.", clientInfo);
    }
  }

  public DirectProcessor<WebSocketMessage> pong() {
    return pongMessageProcessor;
  }

  public void close() {
    if (isConnected) {
      log.info("Trying to close termination for client ''", clientInfo);
      isConnected = false;
      this.messageProcessor.onComplete();
      session.close().subscribe();
      notifyDisconnect();
    }
  }

  public String getClientInfo() {
    return clientInfo;
  }

  public DirectProcessor<String> receive() {
    return messageProcessor;
  }

  public void notifyConnect() {
    this.clientConnectedEventBus.onNext(this);
  }

  public void notifyDisconnect() {
    this.clientDisconnectedEventBus.onNext(this);
  }

  public String getSessionId() {
    return sessionId;
  }

  public boolean isConnected() {
    return isConnected;
  }

  public WebSocketSession getSession() {
    return session;
  }
}
