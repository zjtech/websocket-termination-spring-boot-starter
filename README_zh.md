### websocket-termination-spring-boot-starter
## Websocket终结

如果你的应用是一个websocket的服务端，你需要在收到一个websocket消息时，将该消息传递到后端的服务。可是很不幸的是后端的服务提供的是Restful API, 此时你需要在websocket 服务端先将websocket终结掉，然后将请求转发到后端的Restful服务。此时你可以使用这个项目。

## 这个starter是否适合我的项目？    
你可以考虑是否需要这些功能：    
* 这个项目是基于Spring boot2和webflux的实现
* 我需要在websocket服务端收到请求后随后调用后端的服务并返回处理结果, 这个服务可能只提供了Restful API或只能消费MQ消息    
* 后端的Restful服务可以调用websocket服务端的actuator endpoint，基于Restful API的方式反向下发通知给websocket客户端       
* websocket服务端提供PING/PONG功能，当客户端连接后通过PING/PONG维持心跳 
* 我需要知道当前有多少个客户端连接着，以及对于的session和IP地址    

##### 如果上述这些功能你恰好需要，那可以考虑这个基于spring boot2的无侵入式的boot start库依赖。
                                                                                                                          
  
## 一种可能的应用场景示例
![PIC](https://github.com/zjtech/websocket-termination-spring-boot-starter/blob/master/sample.png)   
 
                                                                                                                                  