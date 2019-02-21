package zjtech.websocket.termination.core;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemorySessionHolder implements ISessionHolder {

  private ConcurrentHashMap<String, SessionHandler> sessionMap = new ConcurrentHashMap<>();

  @Override
  public void cache(SessionHandler sessionHandler) {
    String sessionId = sessionHandler.getSessionId();
    if (sessionMap.contains(sessionId)) {
      log.warn("A duplicate session is detected. {}", sessionHandler.getClientInfo());
    }
    sessionMap.put(sessionId, sessionHandler);
    log.info("Cache a session handler for client '{}'", sessionHandler.getClientInfo());
    log.info("this.sessionHolder={}", this.hashCode());
  }

  @Override
  public SessionHandler getSessionHandler(String sessionId) {
    return sessionMap.get(sessionId);
  }

  @Override
  public SessionHandler remove(String sessionId) {
    SessionHandler sessionHandler = sessionMap.remove(sessionId);
    if (sessionHandler != null) {
      log.info(
          "Remove a session handler from cache and the corresponding client is '{}'",
          sessionHandler.getClientInfo());
    }
    return sessionHandler;
  }

  @Override
  public SessionHandler remove(SessionHandler session) {
    SessionHandler sessionHandler = sessionMap.remove(session.getSessionId());
    log.info(
        "Remove a session handler from cache and the corresponding client is '{}'",
        session.getClientInfo());
    return sessionHandler;
  }

  @Override
  public List<String> getSessionList() {
    return sessionMap.values().stream()
        .map(SessionHandler::getClientInfo)
        .collect(Collectors.toList());
  }
}
