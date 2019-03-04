### websocket-termination-spring-boot-starter
[中文文档]()
## Regarding Websocket Termination
In case you have a websocket server, you need to pass the received websocket message to a backend service. 
But unfortunately the backend service is a restful based service, and only the rest API can be invoked.
Hence you have to terminate the websocket firstly in websocket server side and pass the request to the
backend server. At this point, this spring boot starter is what you may need.

## Is this starter suited for my project?   
You could sonsider the following things:     
* This boot starter project is based on Spring boot2 **(2.1.1.RELEASE and later)** with webflux
* The websocket server should terminate the websocket message and invoke a backend service to get the result to client,
regardless of the backend service is a restful based or message based service
* The backend service could invoke the actuator API(Rest API) of upstream websocket server, to notify a websocket client.
* The websocket server shall support sending or receiving PING/PONG frame in order to keep the connection alive
* You may need to know how many connections established, the details of the websocket session and IP address for a specific websocket client   

##### If all of these features is what you need, you can consider using this non-intrusive and spring boot2 based dependency    
With this start, all you need to do is: 
* You need to define a websocket request and a class to process this request working as a consumer              
Thus you can conveniently get the client request,  and the starter can ensure the request is passed into your consumer class.
Very convenient, right?
                                                                                                                          
  
## A possible usage scenario
![PIC](https://github.com/zjtech/websocket-termination-spring-boot-starter/blob/master/sample.png)   


## How to develop
#### 1. Add this dependency
For gradle, you can add the dependency like this:    
```   
compile "zjtech:websocket-termination-spring-boot-starter:0.1"
```   
#### 2. Enable the webdocket termination function in project's configuration file  
```
websocket:
  termination:
    scan:
      api-package: sample.api   

```
```api-package```this parameter specify what package the webscoket request classes are placed in 

##### The complete configuration, as follows:  
```
websocket:
  termination:
    enabled: true
    endpoint: "/ws"
    order: -1
    ping:
      enabled: true
      interval: 10
      retries: 3
      supress-log: true
    scan:
      api-package: sample.api
```
| Configuration Item                                | Default Value |            Description                    |
|:---------------------------------------------------|:---------:|:----------------------------------------------|
| websocket.termination.enabled                     | true      | Enable WebSocket termination
| websocket.termination.endpoint                    | /ws       | The actuator endpoing. By default, the value is ws://IP:Port/ws        |  
| websocket.termination.order                       | -1        | The sort order for websocket handler mapping
| websocket.termination.ping.enabled                | true      | Enable PING/PONG                                                        |
| websocket.termination.ping.interval               | 10        | In seconds，to specify how oftern the server should send a PING frame to client  |
| websocket.termination.ping.retries                | 3         | The retry count while server cannot get response from client ,and finally close the session| 
| websocket.termination.ping.supress-log            | true      | Whether print the PING/PONG log                                        |
| websocket.termination.ping.scan.api-package       | <NA>      | The package that webscoket request classes are placed in.<br/> NA, **but the developer should specify this package**.|

## A sample project for reference
A demo project is provided you may be interested in, you can clone this project to learn how to implement a websocket server,
and how to terminate the websocket.
https://github.com/zjtech/websocket-termination-demo  

### 3. Create a customized request in a package "zjtech.sample.api" 
This class should extend ```zjtech.websocket.termination.api.Request```      
```
@Getter
@Setter
@WebSocketCommand("CREATE_POLICY")
public class CreatePolicyRequest implements Request {

  private String name;
  private String description;
}
```
### 4. Define a class that can process the above request class        
```
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
```       
Here you can customize a response class, or you can directly implement the ```zjtech.websocket.termination.api.Response``` interface.
The CreatePolicyResponse class used above is a customized class to implement the Response interface, meanwhile you can 
add corresponding fileds as your business needs.
```
@Getter
@Setter
public class CreatePolicyResponse implements BaseResponse {

  private int errorCode = 200;
  private String errorMessage;
  private String command;

  private Payload payload = new Payload();

  @Getter
  @Setter
  public static class Payload {

    private long id;
    private String name;
    private String description;
    private String createTime;
    private String creater;
    private boolean validPolicy;
  }
}

```            
### 5. The websocket client:
After completed the above steps, you need to create a spring boot application class, once the server is started, the websocket function
should be enabled in together. And the CreatePolicyRequest would be processed by RestMessageForwarder class.       
**Note: The client should send the RequestWrapper class instead of CreatePolicyRequest**
the format should looks like this:
```
{
  "command": "CREATE_POLICY",
  "payload": {  
          "name": "policy1"  // payload stores a Request, here it is  CreatePolicyRequest
       }
}
```
Which means the request client sent is to create a policy and the payload will be converted into CreatePolicyRequest.

* The following is a sample of WebSocket Client：
```
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
```
The server log will indicates the following line after the CREATE_POLICY request is sent:
```
11:16:24.492 [reactor-http-epoll-4] INFO reactor.Flux.Map.1 - onNext({"errorCode":201,"errorMessage":"A policy is created successfully.","command":"CREATE_POLICY_RESPONSE","payload":{"id":11223344,"name":"policy1","description":"a policy created in backend service","createTime":"2019-02-28 11:16:24","creater":"admin","validPolicy":true}})
```
* The web browser's websocket client    
Here a chrome plugin "Simple Web Socket Client" is used to illustrate the client can send a websocket message to server side,
and then the message will be passed into consumer class, and finally print the result in GUI.
![Web Browser Client](https://github.com/zjtech/websocket-termination-spring-boot-starter/blob/master/browser_client.gif)

## Advanced   
Other features you may be interested in     
* Actuator Endpoint   
After the dependency ```org.springframework.boot:spring-boot-starter-actuator``` is added, there're two actuator endpoints are enabled.
 
|                 End Point                |            Description     
|:-----------------------------------------|:--------------------------------------------------------------------|
| /actuator/websocketInfo                  | GET /actuator/websocketInfo <br/>Show the internal mapping relationship and connected client info<br/>activeSessionCount: Current active session count<br/>activeSessions: list the sessionId and corresponding client IP address for each session <br/>mapping： Show the mapping relationship from ```Request``` to ```MessageConsumer```    |
| /actuator/websocketOperation/{sessionId} | POST /actuator/websocketOperation/{sessionId}?message=a%20message : <br/>Call this API to send message to websocket client<br/>DELETE /actuator/websocketOperation/{sessionId} : <br/>Close and delete the client session by session id<br/>             |                                                                           |
