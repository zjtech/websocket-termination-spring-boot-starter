package zjtech.websocket.termination.core;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.websocket.termination.api.BaseRequest;
import zjtech.websocket.termination.common.RequestWrapper;
import zjtech.websocket.termination.common.WsConnectionException;
import zjtech.websocket.termination.common.WsErrorCode;
import zjtech.websocket.termination.common.WsUtils;

@Slf4j
@Component
public class RequestParser implements IRequestParser {

  private final WsUtils utils;
  private final CommandMappingHandler mappingHandler;

  @Autowired
  public RequestParser(WsUtils utils, CommandMappingHandler mappingHandler) {
    this.utils = utils;
    this.mappingHandler = mappingHandler;
  }

  public RequestWrapper<? extends BaseRequest> parse(String requestMessage) {
    JsonNode jsonNode = utils.convertJsonNode(requestMessage);
    JsonNode commandNode = jsonNode.get("command");
    if (commandNode == null) {
      log.warn("The command field is missing : {}", requestMessage);
      throw new WsConnectionException(WsErrorCode.COMMAND_REQUIRED);
    }

    String command = commandNode.asText();
    Class<?> payloadClass = mappingHandler.getRequestClass(command);
    if (payloadClass == null) {
      log.info("The command '{}' is unknown. ", command);
      throw new WsConnectionException(WsErrorCode.UNKNOWN_COMMAND);
    }

    JsonNode payloadNode = jsonNode.get("payload");
    if (payloadNode == null) {
      log.info("No payload is provided and the request is : {}", requestMessage);
      return null;
    }
    BaseRequest baseRequest = (BaseRequest) utils.treeToValue(payloadNode, payloadClass);

    RequestWrapper requestWrapper = new RequestWrapper();
    requestWrapper.setCommand(command);
    requestWrapper.setRequest(baseRequest);
    return requestWrapper;
  }
}
