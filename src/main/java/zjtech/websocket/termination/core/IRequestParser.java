package zjtech.websocket.termination.core;

import zjtech.websocket.termination.api.BaseRequest;
import zjtech.websocket.termination.common.RequestWrapper;

public interface IRequestParser {

  RequestWrapper<? extends BaseRequest> parse(String requestMessage);
}
