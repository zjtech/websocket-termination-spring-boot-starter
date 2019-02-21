package zjtech.websocket.termination.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.scheduler.Schedulers;
import zjtech.websocket.termination.actuator.WebSocketConnectionEndPoint;
import zjtech.websocket.termination.common.Constants;
import zjtech.websocket.termination.common.Constants.BeanName;
import zjtech.websocket.termination.common.MyWebExceptionHandler;
import zjtech.websocket.termination.common.WsUtils;
import zjtech.websocket.termination.core.CommandMappingHandler;
import zjtech.websocket.termination.core.DefaultClientConnectionListener;
import zjtech.websocket.termination.core.DefaultWebSocketHandler;
import zjtech.websocket.termination.core.IClientConnectionListener;
import zjtech.websocket.termination.core.IRequestParser;
import zjtech.websocket.termination.core.ISessionHolder;
import zjtech.websocket.termination.core.MemorySessionHolder;
import zjtech.websocket.termination.core.MessageHandler;
import zjtech.websocket.termination.core.PingHandler;
import zjtech.websocket.termination.core.RequestParser;
import zjtech.websocket.termination.core.SessionHandler;

@Slf4j
@ConditionalOnProperty(name = "websocket.termination.enabled", matchIfMissing = true)
@EnableConfigurationProperties(WsConnectionConfigProps.class)
@AutoConfigureOrder
@ConditionalOnClass(DispatcherHandler.class)
// @AutoConfigureAfter(value = {DelegatingWebFluxConfiguration.class})
public class WsConnectionAutoConfigure {

  @Bean
  @ConditionalOnMissingBean
  public WebSocketClient webSocketClient() {
    log.debug("Init a default WebSocketClient");
    return new ReactorNettyWebSocketClient();
  }

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    log.debug("Init a default ObjectMapper");
    return new ObjectMapper();
  }

  @Bean
  @ConditionalOnMissingBean
  public WsUtils wsUtils(ApplicationContext ctx) {
    log.debug("Init a default WsUtils");
    return new WsUtils(objectMapper(), ctx);
  }

  @Bean
  @ConditionalOnMissingBean
  public CommandMappingHandler commandMappingHandler(
      WsConnectionConfigProps connectionConfigProps) {
    log.debug("Init a default CommandMappingHandler");
    return new CommandMappingHandler(connectionConfigProps);
  }

  /**
   * Register a session holder bean for maintaining the websocket session in memory
   *
   * @return ISessionHolder
   */
  @Bean
  public ISessionHolder sessionHolder() {
    log.debug("Init a default ISessionHolder");
    return new MemorySessionHolder();
  }

  @Bean
  @ConditionalOnMissingBean
  public WebSocketHandlerAdapter handlerAdapter() {
    log.debug("Init a default WebSocketHandlerAdapter");
    return new WebSocketHandlerAdapter();
  }

  @Bean
  @ConditionalOnMissingBean
  public WebSocketHandler webSocketHandler(WsUtils wsUtils) {
    log.debug("Init a default WebSocketHandler");
    return new DefaultWebSocketHandler(wsUtils);
  }

  /**
   * Initialize a handler mapping for websocket
   *
   * @param connectionConfigProps WsConnectionConfigProps
   * @return HandlerMapping
   */
  @Bean
  public HandlerMapping webSocketHandlerMapping(
      WsConnectionConfigProps connectionConfigProps, WsUtils utils) {
    log.info("Expose websocket endpoint to {}", connectionConfigProps.getEndpoint());
    // how many endpoints should be registered here?? TODO
    // https://stackoverflow.com/questions/17280455/which-is-better-multiple-web-socket-endpoints-or-single-web-socket-endpoint-in
    Map<String, WebSocketHandler> map = new HashMap<>();
    map.put(connectionConfigProps.getEndpoint(), webSocketHandler(utils));

    SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
    handlerMapping.setOrder(connectionConfigProps.getOrder());
    handlerMapping.setUrlMap(map);
    return handlerMapping;
  }

  @Bean
  public IClientConnectionListener clientConnectionListener() {
    return new DefaultClientConnectionListener(sessionHolder());
  }

  /*  @Bean(name = Constants.BeanName.clientConnectedEventBus)
  public EmitterProcessor<SessionHandler> clientConnectedEventBus(
      IClientConnectionListener clientConnectionListener) {
    EmitterProcessor<SessionHandler> processor = EmitterProcessor.create();
    processor.subscribeOn(Schedulers.elastic()).subscribe(clientConnectionListener::connect);
    return processor;
  }*/

  @Bean(name = BeanName.clientDisConnectedEventBus)
  public EmitterProcessor<SessionHandler> clientDisConnectedEventBus(
      IClientConnectionListener clientConnectionListener) {
    EmitterProcessor<SessionHandler> processor = EmitterProcessor.create();
    processor.subscribeOn(Schedulers.elastic()).subscribe(clientConnectionListener::disConnect);
    return processor;
  }

  @Bean
  @Scope(value = "prototype")
  public MessageHandler messageHandler(
      WsUtils wsUtils, CommandMappingHandler commandMappingHandler) {
    return new MessageHandler(
        requestParser(wsUtils, commandMappingHandler), commandMappingHandler, wsUtils);
  }

  @Bean
  @Scope(value = "prototype")
  public SessionHandler sessionHandler(
      WsUtils wsUtils,
      WsConnectionConfigProps configProps,
      @Qualifier(Constants.BeanName.clientConnectedEventBus)
          EmitterProcessor<SessionHandler> clientConnectedEventBus,
      @Qualifier(Constants.BeanName.clientDisConnectedEventBus)
          EmitterProcessor<SessionHandler> clientDisconnectedEventBus) {
    return new SessionHandler(
        wsUtils, configProps, clientConnectedEventBus, clientDisconnectedEventBus);
  }

  @Bean
  @Scope(value = "prototype")
  @ConditionalOnProperty(name = "websocket.termination.ping.enabled", matchIfMissing = true)
  public PingHandler pingPongHandler(WsConnectionConfigProps configProps) {
    return new PingHandler(configProps);
  }

  @Bean
  public IRequestParser requestParser(
      WsUtils wsUtils, CommandMappingHandler commandMappingHandler) {
    return new RequestParser(wsUtils, commandMappingHandler);
  }

  /** Actuator endpoint */
  @Bean
  @ConditionalOnEnabledEndpoint
  public WebSocketConnectionEndPoint webSocketConnectionEndPoint(
      CommandMappingHandler mappingHandler, ISessionHolder sessionHolder) {
    return new WebSocketConnectionEndPoint(sessionHolder, mappingHandler);
  }

  @Bean
  @Order(0)
  @ConditionalOnProperty(
      value = "websocket.termination.enable-web-error-handler",
      matchIfMissing = true)
  public WebExceptionHandler exceptionHandler() {
    log.info("enable a default WebExceptionHandler");
    return new MyWebExceptionHandler();
  }
}
