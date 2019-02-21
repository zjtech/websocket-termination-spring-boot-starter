package zjtech.websocket.termination.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import zjtech.websocket.termination.api.WebSocketCommand;
import zjtech.websocket.termination.config.WsConnectionConfigProps;

@Slf4j
public class CommandMappingHandler implements ApplicationContextAware {

  private final WsConnectionConfigProps configProps;
  private Map<String, Class> comandMap = new HashMap<>(); // command <---> BaseRequest
  private Map<String, ConsumerInfo> consumerMap = new HashMap<>();
  private ApplicationContext context;

  public CommandMappingHandler(WsConnectionConfigProps configProps) {
    this.configProps = configProps;
  }

  /** Find the custom annotations. http://farenda.com/spring/find-annotated-classes/ */
  @PostConstruct
  public void init() {
    scanApiPackage();
    scanMessageConsumer();
  }

  private void scanApiPackage() {
    doScan(
        configProps.getScan().getApiPackage(),
        WebSocketCommand.class,
        cls -> {
          WebSocketCommand webSocketCommand = cls.getAnnotation(WebSocketCommand.class);
          String clientCommand = webSocketCommand.value();
          if (comandMap.containsKey(clientCommand)) {
            log.error(
                "Duplicate command '{}' detected and it used for multiple request class.",
                clientCommand);
          } else {
            comandMap.put(clientCommand, cls);
          }
          log.info(
              "Found class: {} that corresponds to command name:{}",
              cls.getSimpleName(),
              webSocketCommand.value());
        });
  }

  private void scanMessageConsumer() {
    Map<String, Object> map = context.getBeansWithAnnotation(MessageConsumer.class);
    if (map == null) {
      log.info("No websocket message consumer found.");
      return;
    }
    Class cls;
    Consume consumeAnnotation;
    String command;
    for (Object obj : map.values()) {
      cls = obj.getClass();
      for (Method method : obj.getClass().getDeclaredMethods()) {
        consumeAnnotation = AnnotationUtils.findAnnotation(method, Consume.class);
        if (consumeAnnotation != null) {
          command = consumeAnnotation.value();
          if (consumerMap.containsKey(command)) {
            log.warn(
                "Multiple @Consume(command={}) annotations found but only the last one is used.",
                command);
          }
          consumerMap.put(command, new ConsumerInfo(cls, method));
          log.info(
              "Mapped websocket message consumer '{}' with command '{}'",
              method.toGenericString(),
              command);
        }
      }
    }
  }

  private void doScan(
      String scanPackage,
      Class<? extends Annotation> annotationClass,
      Consumer<Class<?>> consumer) {
    ClassPathScanningCandidateComponentProvider provider =
        new ClassPathScanningCandidateComponentProvider(false);
    provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
    for (BeanDefinition beanDef : provider.findCandidateComponents(scanPackage)) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        consumer.accept(cl);
      } catch (Exception e) {
        log.error("An unexpected exception occurs while scanning package '{}'", e, scanPackage);
      }
    }
  }

  /**
   * Get entity class by command
   *
   * @param command Command
   * @return Class
   */
  public Class<?> getRequestClass(String command) {
    Objects.requireNonNull(command);
    return comandMap.get(command);
  }

  public ConsumerInfo getConsumeInfo(String type) {
    return consumerMap.get(type);
  }

  public Map<String, Object> getMapingInfo() {
    Map<String, Object> map = new HashMap<>();
    map.put("requestCommandMapping", Collections.unmodifiableMap(comandMap));

    Map<String, String> methodMappingMap = new HashMap<>();
    for (Map.Entry<String, ConsumerInfo> entry : consumerMap.entrySet()) {
      methodMappingMap.put(entry.getKey(), entry.getValue().getMethod().toGenericString());
    }
    map.put("consumerMethodMapping", methodMappingMap);
    return map;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class ConsumerInfo {

    private Class<?> consumeClass;
    private Method method;
  }
}
