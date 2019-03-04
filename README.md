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

### 3. Create a customized request in a package "sample.api" 
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
### 4. 定义一个可以处理该消息的类        
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
你可以自定义一个Response类，也可以实现框架中提供的Response接口。上面使用的CreatePolicyResponse即为自定义实现了Response接口的类，你可以按需添加对应业务场景的属性。         
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
### 5. 客户端连接并发送请求      
在完成以上步骤后，你需要一个Spring boot 的启动类。启动后，webSocket功能会被启用，而且可以将CreatePolicyRequest请求交由RestMessageForwarder类处理。
**注意: 客户端的请求对象必须为RequestWrapper**
```
{
  "command": "CREATE_POLICY",
  "payload": {
          "name": "policy1"
       }
}
```
如果使用的是基于java的webScoket客户端，可以在客户端发送一个zjtech.websocket.termination.common.RequestWrapper对象                                                                                                   

* 以下是一个WebSocket Client的例子：
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
当发送了CREATE_POLICY请求后，会将服务端的响应结果在日志中显示：
```
11:16:24.492 [reactor-http-epoll-4] INFO reactor.Flux.Map.1 - onNext({"errorCode":201,"errorMessage":"A policy is created successfully.","command":"CREATE_POLICY_RESPONSE","payload":{"id":11223344,"name":"policy1","description":"a policy created in backend service","createTime":"2019-02-28 11:16:24","creater":"admin","validPolicy":true}})
```
* 浏览器WebSocket客户端    
这里使用了chrome浏览器上的插件"Simple Web Socket Client", 演示客户端发送消息，并且消息会传递到消费者中处理，同时返回结果给客户端。   
![Web Browser Client](https://github.com/zjtech/websocket-termination-spring-boot-starter/blob/master/browser_client.gif)

## 进阶    
以下部分会介绍其他特性，也许你会感兴趣。   
* Actuator Endpoint   
当工程中添加了依赖org.springframework.boot:spring-boot-starter-actuator后有两个endpoint.     
 
|                 End Point                |            描述                                                      |
|:-----------------------------------------|:--------------------------------------------------------------------|
| /actuator/websocketInfo                  | 显示当前框架内部的Mapping关系和连接的客户端信息<br/>activeSessionCount: 当前活跃中的Session数<br/>activeSessions:  列表显示每个Session相关的sessionId以及客户端的IP地址信息<br/>mapping： 显示Request和MessageConsumer的映射关系                        |
| /actuator/websocketOperation/{sessionId} | POST /actuator/websocketOperation/{sessionId}?message=a%20message : <br/>调用此接口向客户端发送通知消息<br/>DELETE /actuator/websocketOperation/{sessionId} : <br/>根据session id在WebSocket 服务端关闭与客户端的Session<br/>             |                                                                           |
