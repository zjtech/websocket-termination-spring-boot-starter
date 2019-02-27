package zjtech.websocket.termination.core;

import zjtech.websocket.termination.common.RequestWrapper;

public interface IRequestParser {

  RequestWrapper parse(String requestMessage);
}
