package zjtech.websocket.termination.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "websocket.termination")
@Getter
@Setter
public class WsConnectionConfigProps {

  private boolean enabled = true;
  private String endpoint = "/ws";
  private int order = -1;
  private PingConfigProps ping = new PingConfigProps();
  private ScanPackage scan = new ScanPackage();
  private boolean enableWebErrorHandler = false;

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
