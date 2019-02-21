package zjtech.websocket.termination.core;

import zjtech.websocket.termination.api.BaseRequest;

public interface IConsumerContext {

  SessionHandler getSessionHandler();

  BaseRequest getRequest();

  String getCommand();
}
