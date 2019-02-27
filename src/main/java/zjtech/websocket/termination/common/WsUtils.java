package zjtech.websocket.termination.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.context.ApplicationContext;

public class WsUtils {

  private final ObjectMapper objectMapper;

  private final ApplicationContext ctx;

  public WsUtils(ObjectMapper objectMapper, ApplicationContext ctx) {
    this.objectMapper = objectMapper;
    this.ctx = ctx;
  }

  public <T> T getBean(Class<T> beanClass) {
    return ctx.getBean(beanClass);
  }

  /**
   * Convert the JSON string to JSON node.
   *
   * @param jsonData json string data
   * @return JsonNode
   */
  public JsonNode convertJsonNode(String jsonData) {
    try {
      return objectMapper.readTree(jsonData);
    } catch (IOException e) {
      throw new WsConnectionException(WsErrorCode.INVALID_JSON_DATA, e);
    }
  }

  /**
   * Bind data given JSON tree * contains into specific value.
   *
   * @param node TreeNode
   * @param valueType Class
   * @return value
   */
  public <T> T treeToValue(TreeNode node, Class<T> valueType) {
    try {
      return objectMapper.treeToValue(node, valueType);
    } catch (JsonProcessingException e) {
      throw new WsConnectionException(WsErrorCode.INVALID_JSON_DATA, e);
    }
  }

  /**
   * Convert a object to string data.
   *
   * @param valueObject object
   * @return string data
   */
  public <T> String convertString(T valueObject) {
    try {
      return objectMapper.writeValueAsString(valueObject);
    } catch (JsonProcessingException e) {
      throw new WsConnectionException(WsErrorCode.INVALID_JSON_DATA, e);
    }
  }
}
