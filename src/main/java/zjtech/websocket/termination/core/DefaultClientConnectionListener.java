package zjtech.websocket.termination.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultClientConnectionListener implements IClientConnectionListener {

  private final ISessionHolder sessionHolder;

  public DefaultClientConnectionListener(ISessionHolder sessionHolder) {
    this.sessionHolder = sessionHolder;
  }

  @Override
  public void connect(SessionHandler sessionHandler) {
    log.info("A client '{}' is connected.", sessionHandler.getClientInfo());
    sessionHolder.cache(sessionHandler);
  }

  @Override
  public void disConnect(SessionHandler sessionHandler) {
    log.info("A client '{}' is disconnected.", sessionHandler.getClientInfo());
    sessionHolder.remove(sessionHandler);
  }
}
