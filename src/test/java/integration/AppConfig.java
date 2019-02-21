package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import zjtech.websocket.termination.common.WsUtils;

// @Configuration
@Slf4j
public class AppConfig {

  //  @Bean
  //  @Primary
  public WsUtils wsUtils(ObjectMapper objectMapper, ApplicationContext ctx) {
    log.info("Init a custom WsUtils");
    return new CustomWsUtils(objectMapper, ctx);
  }

  //  @Bean
  public ObjectMapper objectMapper() {
    log.info("Init a custom ObjectMapper");
    return new ObjectMapper2();
  }

  private class ObjectMapper2 extends ObjectMapper {}

  public class CustomWsUtils extends WsUtils {

    public CustomWsUtils(ObjectMapper objectMapper, ApplicationContext ctx) {
      super(objectMapper, ctx);
    }
  }
}
