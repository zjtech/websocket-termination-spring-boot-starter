package zjtech.websocket.termination.core;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import zjtech.websocket.termination.config.WsConnectionConfigProps;

@Slf4j
public class PingPongHandler {
  private AtomicInteger pingCount = new AtomicInteger(0);
  private WsConnectionConfigProps configProps;

  public PingPongHandler(WsConnectionConfigProps configProps) {
    this.configProps = configProps;
  }

  /**
   * Asynchronously handle PING/PONG message.
   *
   * @param sessionHandler SessionHandler
   */
  public void asyncHandle(SessionHandler sessionHandler) {
    if (!configProps.getPing().isEnabled()) {
      log.info("The PING/PONG messages are not enabled.");
      return;
    }

    // handle PONG message
    sessionHandler
        .pong()
        .doOnNext(
            msg -> {
              pingCount.set(0); // reset it
              if (!configProps.getPing().isSupressLog()) {
                log.info("Got a PONG message from client '{}'", sessionHandler.getClientInfo());
              }
            })
        .subscribe();

    WebSocketSession session = sessionHandler.getSession();
    Flux.interval(Duration.ofSeconds(configProps.getPing().getInterval()))
        .takeUntil(value -> this.shouldComplete(value, sessionHandler))
        .map(
            val ->
                session.pingMessage(
                    dataBufferFactory -> dataBufferFactory.wrap(("ping" + val).getBytes())))
        .subscribeOn(Schedulers.elastic())
        .doOnNext(
            data -> {
              sessionHandler.send(data);
              pingCount.getAndIncrement();
              if (!configProps.getPing().isSupressLog()) {
                log.info("Sent a PING message to client '{}'", sessionHandler.getClientInfo());
              }
            })
        .doOnTerminate(
            () ->
                // if the the client is disconnected or no PONG message got
                // after multiple PING messages have been sent
                log.info(
                    "Stopped the timer for sending PING message to client '{}' ",
                    sessionHandler.getClientInfo()))
        .subscribe();
  }

  private boolean shouldComplete(long value, SessionHandler sessionHandler) {
    if (!sessionHandler.isConnected()) {
      return true;
    }
    int currentRetries = pingCount.get();
    if (currentRetries >= configProps.getPing().getRetries()) {
      log.warn(
          "Cannot get PONG message for {} times, the client could be disconnected.",
          configProps.getPing().getRetries());
      sessionHandler.close();
      return true;
    }
    return false;
  }
}
