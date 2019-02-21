package zjtech.websocket.termination.core;

public interface IClientConnectionListener {

  public void connect(SessionHandler sessionHandler);

  public void disConnect(SessionHandler sessionHandler);
}
