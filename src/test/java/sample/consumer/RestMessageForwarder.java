package sample.consumer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sample.api.CreatePolicyRequest;
import sample.api.CreatePolicyResponse;
import zjtech.websocket.termination.core.Consume;
import zjtech.websocket.termination.core.ConsumerContext;
import zjtech.websocket.termination.core.MessageConsumer;

@Slf4j
@Component
@MessageConsumer
public class RestMessageForwarder {

  @Consume("CREATE_POLICY")
  public void createPolicy(ConsumerContext<CreatePolicyRequest> ctx) {
    //get the payload
    CreatePolicyRequest request= ctx.getPayload();

    // you can forward the payload to backend service
    log.info("forward a CreatePolicyRequest to backend rest service.");

    //then you construct a payload returned form backend service like this:
    CreatePolicyResponse.Payload payload = new CreatePolicyResponse.Payload();
    payload.setCreater("admin");
    payload.setCreateTime(
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
    payload.setDescription("a policy created in backend service");
    payload.setId(11223344L);
    payload.setName("policy1");
    payload.setValidPolicy(true);

    // and then construct and send a response to client
    CreatePolicyResponse response = new CreatePolicyResponse();
    response.setErrorCode(201);
    response.setErrorMessage("A policy is created successfully.");
    response.setCommand("CREATE_POLICY_RESPONSE");
    response.setPayload(payload);

    //send now
    ctx.getSessionHandler().sendJsonString(response);
  }
}
