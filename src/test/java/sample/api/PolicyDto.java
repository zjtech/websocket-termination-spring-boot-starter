package sample.api;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PolicyDto {

  private String id;
  private String name;
  private String description;
  private LocalDateTime createTime;
  private String creater;
  private boolean validPolicy;
}
