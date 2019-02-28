package javaclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import org.junit.Test;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;
import sample.api.CreatePolicyRequest;
import zjtech.websocket.termination.common.RequestWrapper;

public class JavaClient {

  @Test
  public void runClient() {
    ObjectMapper objectMapper = new ObjectMapper();

    CreatePolicyRequest request = new CreatePolicyRequest();
    request.setName("policy");

    RequestWrapper<CreatePolicyRequest> requestWrapper = new RequestWrapper<>();
    requestWrapper.setPayload(request);
    requestWrapper.setCommand("CREATE_POLICY");
    //    requestWrapper.setHeader();

    WebSocketClient client = new ReactorNettyWebSocketClient();
    client
        .execute(
            URI.create("ws://localhost:5809/ws"),
            session -> {
              try {
                return session
                    .send(
                        Mono.just(
                            session.textMessage(objectMapper.writeValueAsString(requestWrapper))))
                    .thenMany(session.receive().map(WebSocketMessage::getPayloadAsText).log())
                    .then();
              } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Mono.empty();
              }
            })
        .block(Duration.ofSeconds(10L));
  }
}
