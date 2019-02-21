package zjtech.websocket.termination.core;

import java.util.List;

public interface ISessionHolder {

  void cache(SessionHandler sessionHandler);

  SessionHandler getSessionHandler(String sessionId);

  SessionHandler remove(String sessionId);

  SessionHandler remove(SessionHandler sessionHandler);

  List<String> getSessionList();
}
