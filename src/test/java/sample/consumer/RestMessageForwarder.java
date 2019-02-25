package sample.consumer;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sample.api.CreatePolicyResponse;
import zjtech.websocket.termination.core.Consume;
import zjtech.websocket.termination.core.ConsumerContext;
import zjtech.websocket.termination.core.MessageConsumer;

@Slf4j
@Component
@MessageConsumer
public class RestMessageForwarder {

  @Consume("CREATE_POLICY")
  public void createPolicy(ConsumerContext ctx) {
    // you can forward the message to backend service
    log.info("forward a CreatePolicyRequest request to backend rest service.");
    CreatePolicyResponse.Payload payload = new CreatePolicyResponse.Payload();
    payload.setCreater("admin");
    payload.setCreateTime(LocalDateTime.now());
    payload.setDescription("a policy created in backend service");
    payload.setId(11223344L);
    payload.setName("policy1");
    payload.setValidPolicy(true);

    // and then send response to client
    CreatePolicyResponse response = new CreatePolicyResponse();
    response.setErrorCode(201);
    response.setErrorMessage("A policy is created successfully.");
    response.setCommand("CREATE_POLICY_RESPONSE");
    response.setPayload(payload);

    ctx.getSessionHandler().sendJsonString(response);
  }
}
