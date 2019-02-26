package zjtech.websocket.termination.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "websocket.termination")
@Getter
@Setter
public class WsConnectionConfigProps {

  private boolean enabled = true;
  private String endpoint = "/ws";
  private int order = -1;
  private PingConfigProps ping = new PingConfigProps();
  private ScanPackage scan = new ScanPackage();

  @Getter
  @Setter
  public class PingConfigProps {

    private boolean enabled = true;
    private int interval = 10;
    private int retries = 3;
    private boolean supressLog = true;
  }

  @Getter
  @Setter
  public class ScanPackage {

    private String apiPackage;
  }
}
