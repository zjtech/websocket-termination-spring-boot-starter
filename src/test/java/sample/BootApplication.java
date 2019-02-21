package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Make this project as executable boot application for test so that the @SpringBootTest can be
 * used.
 */
@SpringBootApplication
@EnableWebFlux
//@Import(AppConfig.class)
public class BootApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootApplication.class, args);
  }
}
