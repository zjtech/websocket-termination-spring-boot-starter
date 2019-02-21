package integration.consumer;

import integration.api.CreatePolicyRequest;
import integration.api.CreatePolicyResponse;
import integration.api.DeletePolicyRequest;
import integration.api.DeletePolicyResponse;
import integration.api.PolicyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import zjtech.websocket.termination.core.Consume;
import zjtech.websocket.termination.core.ConsumerContext;
import zjtech.websocket.termination.core.MessageConsumer;

@Slf4j
@Component
@MessageConsumer
public class RestMessageForwarder {

  private WebClient client;

  private int internalError = 600;

  public RestMessageForwarder() {
    client = WebClient.create();
  }

  @Consume("CREATE_POLICY")
  public void createPolicy(ConsumerContext ctx) {
    CreatePolicyRequest request = (CreatePolicyRequest) ctx.getRequest();
    BeanCopier copier =
        BeanCopier.create(PolicyDto.class, CreatePolicyResponse.Payload.class, false);

    // post the message to backend service
    log.info("forward a CreatePolicyRequest request to backend rest service.");

    // return this object to client
    client
        .post()
        .uri("http://10.113.49.230:5801/api/v1/policies")
        .accept(MediaType.APPLICATION_JSON)
        .body(Mono.just(request), CreatePolicyRequest.class)
        .retrieve() // or using exchange()
        .bodyToMono(PolicyDto.class)
        .map(
            policyDto -> {
              // convert to response
              CreatePolicyResponse response = new CreatePolicyResponse();
              copier.copy(policyDto, response.getPayload(), null);
              return response;
            })
        .doOnNext(response -> ctx.getSessionHandler().sendJsonString(response))
        .doOnError(
            thr -> {
              CreatePolicyResponse baseResponse = new CreatePolicyResponse();
              baseResponse.setErrorCode(internalError);
              baseResponse.setErrorMessage(thr.getMessage());
              ctx.getSessionHandler().sendJsonString(baseResponse);
            })
        .subscribe();
  }

  @Consume("DELETE_POLICY")
  public void deletePolicy(ConsumerContext ctx) {
    // post the message to backend service
    log.info("forward a DeletePolicyRequest request to backend rest service.");
    String id = ((DeletePolicyRequest) ctx.getRequest()).getId();

    // return this object to client
    client
        .delete()
        .uri("http://10.113.49.230:5801/api/v1/policies/" + id)
        .accept(MediaType.APPLICATION_JSON)
        .exchange() // or using exchange()
        .map(
            response -> {
              DeletePolicyResponse baseResponse = new DeletePolicyResponse();
              baseResponse.setErrorCode(response.statusCode().value());
              baseResponse.setErrorMessage(response.statusCode().name());
              return baseResponse;
            })
        .doOnNext(response -> ctx.getSessionHandler().sendJsonString(response))
        .subscribe();
  }
}
