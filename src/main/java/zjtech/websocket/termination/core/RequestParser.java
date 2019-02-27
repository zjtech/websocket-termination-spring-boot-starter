package zjtech.websocket.termination.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.websocket.termination.api.Request;
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

  /**
   * Parse the message in text format and finally construct a RequestWrapper object.
   *
   * @param requestMessage text message
   * @return RequestWrapper
   */
  public RequestWrapper parse(String requestMessage) {
    RequestWrapper<Request> requestWrapper = new RequestWrapper<>();

    JsonNode jsonNode = utils.convertJsonNode(requestMessage);
    JsonNode commandNode = jsonNode.get("command");
    if (commandNode == null) {
      log.warn("The command field is missing : {}", requestMessage);
      throw new WsConnectionException(WsErrorCode.COMMAND_REQUIRED);
    }

    String command = commandNode.asText();
    Class<?> payloadClass = mappingHandler.getRequestClass(command);
    if (payloadClass == null) {
      log.warn("The command '{}' is unknown.", command);
      throw new WsConnectionException(WsErrorCode.UNKNOWN_COMMAND);
    }

    JsonNode headerNode = jsonNode.get("header");
    if (headerNode != null) {
      Map headers = utils.treeToValue(headerNode, Map.class);
      requestWrapper.setHeader(headers);
    }

    JsonNode payloadNode = jsonNode.get("payload");
    if (payloadNode == null) {
      log.info("No payload is provided and the request is : {}", requestMessage);
      return null;
    }
    Request baseRequest = (Request) utils.treeToValue(payloadNode, payloadClass);

    requestWrapper.setCommand(command);
    requestWrapper.setPayload(baseRequest);
    return requestWrapper;
  }
}
