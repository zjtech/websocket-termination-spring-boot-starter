package zjtech.websocket.termination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.BootApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import zjtech.websocket.termination.common.WsUtils;
import zjtech.websocket.termination.config.WsConnectionConfigProps;

@RunWith(SpringRunner.class)
// @ContextConfiguration(classes = WsConnectionAutoConfigure.class)
@SpringBootTest(classes = BootApplication.class)
public class WsConnectionAutoTest {

  @Autowired private WsUtils wsUtils;

  @Autowired private WsConnectionConfigProps connectionConfigProps;

  @Autowired private ObjectMapper objectMapper;

  //  @Value("${base.package}")
  private String basePackage;

  @Test
  public void testContext() throws JsonProcessingException {
    System.out.println(wsUtils);
    System.out.println("props=" + objectMapper.writeValueAsString(connectionConfigProps));
    System.out.println("basePackage=" + this.basePackage);
  }
}
