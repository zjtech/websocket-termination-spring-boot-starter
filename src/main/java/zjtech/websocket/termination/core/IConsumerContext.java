package zjtech.websocket.termination.core;

public interface IConsumerContext<T> {

  SessionHandler getSessionHandler();

  T getPayload();

  String getCommand();
}
