### websocket-termination-spring-boot-starter
## Websocket的终结

如果你的应用是一个websocket的服务端，你需要在收到一个websocket消息时，将该消息传递到后端的服务。可是很不幸的是后端的服务提供的是Restful API, 此时你需要在websocket 服务端先将websocket终结掉，然后将请求转发到后端的Restful服务。此时你可以使用这个项目。

## 这个starter是否适合我的项目？    
你可以考虑是否需要这些功能：    
* 这个项目是基于Spring boot2 **(2.1.1.RELEASE 及以上版本)** 和webflux的实现
* Websocket服务端收到请求后调用后端的服务并返回处理结果, 这个服务可能只提供了Restful API或只能消费MQ消息    
* 后端的Restful服务可以调用websocket服务端的actuator endpoint，基于Restful API的方式反向下发通知给websocket客户端       
* websocket服务端提供PING/PONG功能，当客户端连接后通过PING/PONG维持心跳 
* 我需要知道当前有多少个客户端连接着，以及对应的session和IP地址    

##### 如果上述这些功能你恰好需要，那可以考虑这个基于spring boot2的无侵入式的boot start库依赖。    
有了这个starter,你需要做的仅仅是:  
**你仅需要提供一个请求对象，并定义一个处理该对象的类**      
那么你就可以很方便的获得客服端发送过来的请求，并且该框架将会把这个对象转到你的消费类中进行处理。很方便,是吧?
                                                                                                                          
  
## 一种可能的应用场景示例
![PIC](https://github.com/zjtech/websocket-termination-spring-boot-starter/blob/master/sample.png)   

## 

## 如何开发
#### 1. 添加依赖    
对于gradle, 可以添加如下依赖
```   
compile "zjtech:websocket-termination-spring-boot-starter:0.1"
```   
#### 2. 在项目的配置文件中启用    
```
websocket:
  termination:
    scan:
      api-package: sample.api   

```
```api-package```指定了客户端与服务端WebSocket消息对象的存放路径

##### 完整的配置项如下所示:   
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
| 配置项                                             |   默认值   |            描述                     |
|:---------------------------------------------------|:---------:|:----------------------------------------------|
| websocket.termination.enabled                     | true      | 是否启用WebSocket终结功能
| websocket.termination.endpoint                    | /ws       | 客户端连接的端点，默认是 ws://IP:Port/ws                               |  
| websocket.termination.order                       | -1        | 
| websocket.termination.ping.enabled                | true      | 启用PING/PONG                                                        |
| websocket.termination.ping.interval               | 10        | 单位秒，每隔多少秒服务端向客户端发送一次PING报文                           |
| websocket.termination.ping.retries                | 3         | 当服务端发送了PING后，无法收到客户端的响应，最多尝试几次。并最终关闭session。 | 
| websocket.termination.ping.supress-log            | true      | 是否打印PING发送和PONG接收的日志
| websocket.termination.ping.scan.api-package       | <NA>      | 客户端与服务端通信的请求对象所处的包路径，需要开发人员自行指定，无默认值       |

### 3. 在sample.api包中添加自定义的请求对象  
这个类要继承```zjtech.websocket.termination.api.Request```类           
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
你可以自定义一个Response类，也可以实现框架中提供的Response接口。上面使用的CreatePolicyResponse即为自定义实现了Response接口的类，
你可以按需添加对应业务的属性。         
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

## **Demo工程**
这里提供了一个demo工程，你可以参考是如何实现一个WebSocket服务端，如何终结Websocket的。    
https://github.com/zjtech/websocket-termination-demo  

## 进阶    
以下部分会介绍其他特性，也许你会感兴趣。   
* Actuator Endpoint   
当工程中添加了依赖org.springframework.boot:spring-boot-starter-actuator后有两个endpoint.     
 
|                 End Point                |            描述                                                      |
|:-----------------------------------------|:--------------------------------------------------------------------|
| /actuator/websocketInfo                  | 显示当前框架内部的Mapping关系和连接的客户端信息<br/>activeSessionCount: 当前活跃中的Session数<br/>activeSessions:  列表显示每个Session相关的sessionId以及客户端的IP地址信息<br/>mapping： 显示Request和MessageConsumer的映射关系                        |
| /actuator/websocketOperation/{sessionId} | POST /actuator/websocketOperation/{sessionId}?message=a%20message : <br/>调用此接口向客户端发送通知消息<br/>DELETE /actuator/websocketOperation/{sessionId} : <br/>根据session id在WebSocket 服务端关闭与客户端的Session<br/>             |                                                                           |
