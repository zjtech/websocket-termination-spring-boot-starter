package zjtech.websocket.termination.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;
import zjtech.websocket.termination.api.ErrorResponse;
import zjtech.websocket.termination.api.Request;
import zjtech.websocket.termination.common.RequestWrapper;
import zjtech.websocket.termination.common.WsConnectionException;
import zjtech.websocket.termination.common.WsErrorCode;
import zjtech.websocket.termination.common.WsUtils;
import zjtech.websocket.termination.core.CommandMappingHandler.ConsumerInfo;

@Slf4j
public class MessageHandler {

  private final IRequestParser requestParser;
  private final CommandMappingHandler mappingHandler;
  private final WsUtils wsUtils;

  /**
   * Constructor.
   *
   * @param requestParser RequestParser
   * @param mappingHandler CommandMappingHandler
   * @param wsUtils WsUtils
   */
  public MessageHandler(
      IRequestParser requestParser, CommandMappingHandler mappingHandler, WsUtils wsUtils) {
    this.requestParser = requestParser;
    this.mappingHandler = mappingHandler;
    this.wsUtils = wsUtils;
  }

  /**
   * Get the message and construct a ConsumerContext for consumer.
   *
   * @param sessionHandler SessionHandler
   * @return Mono
   */
  public Mono<Void> handle(SessionHandler sessionHandler) {
    // asynchronously consume message in an thread pool
    return sessionHandler
        .receive()
        //        .subscribeOn(Schedulers.elastic())
        .doOnNext(
            receivedMessage ->
                Mono.just(receivedMessage)
                    .map(value -> requestParser.parse(receivedMessage))
                    .map(request -> constructContext(sessionHandler, request))
                    .doOnNext(this::invokeConsumer)
                    .doOnError(throwable -> handleError(sessionHandler, receivedMessage, throwable))
                    .subscribe())
        .then();
  }

  private void invokeConsumer(IConsumerContext context) {
    ConsumerInfo consumeInfo = mappingHandler.getConsumeInfo(context.getCommand());
    Object instance = wsUtils.getBean(consumeInfo.getConsumeClass());
    if (instance == null) {
      log.warn(
          "No consumer found for consuming the message associated with command type '{}'",
          context.getCommand());
      return;
    }
    ReflectionUtils.invokeMethod(consumeInfo.getMethod(), instance, context);
  }

  private IConsumerContext constructContext(SessionHandler sessionHandler, Request request) {
    RequestWrapper requestWrapper = (RequestWrapper) request;

    ConsumerContext<Request> consumerContext = new ConsumerContext<>();
    consumerContext.setSessionHandler(sessionHandler);
    consumerContext.setCommand(requestWrapper.getCommand());
    consumerContext.setHeaders(requestWrapper.getHeader());
    consumerContext.setPayload(requestWrapper.getPayload());
    return consumerContext;
  }

  private void handleError(
      SessionHandler sessionHandler, String receivedMessage, Throwable throwable) {
    ErrorResponse errorResponse = new ErrorResponse();
    if (throwable instanceof WsConnectionException) {
      log.warn(
          "Received a unknown message from client '{}' and the message is '{}'",
          sessionHandler.getClientInfo(),
          receivedMessage,
          ((WsConnectionException) throwable).getRawException());

      WsErrorCode errorCode = ((WsConnectionException) throwable).getErrorCode();
      errorResponse.setErrorCode(400);
      errorResponse.setErrorMessage(errorCode.name());
    } else {
      String errorMsg =
          String.format(
              "An internal exception occurs for session {%s}, the message is: %s ",
              sessionHandler.getSessionId(), receivedMessage);
      log.warn(errorMsg, throwable);
      errorResponse.setErrorCode(500);
      errorResponse.setErrorMessage(throwable.getMessage());
    }
    // send the response
    sessionHandler.sendJsonString(errorResponse);
  }
}
