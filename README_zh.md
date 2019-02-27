### websocket-termination-spring-boot-starter
## Websocket终结

如果你的应用是一个websocket的服务端，你需要在收到一个websocket消息时，将该消息传递到后端的服务。可是很不幸的是后端的服务提供的是Restful API, 此时你需要在websocket 服务端先将websocket终结掉，然后将请求转发到后端的Restful服务。此时你可以使用这个项目。

## 这个starter是否适合我的项目？    
你可以考虑是否需要这些功能：    
* 这个项目是基于Spring boot2和webflux的实现
* 我需要在websocket服务端收到请求后随后调用后端的服务并返回处理结果, 这个服务可能只提供了Restful API或只能消费MQ消息    
* 后端的Restful服务可以调用websocket服务端的actuator endpoint，基于Restful API的方式反向下发通知给websocket客户端       
* websocket服务端提供PING/PONG功能，当客户端连接后通过PING/PONG维持心跳 
* 我需要知道当前有多少个客户端连接着，以及对应的session和IP地址    

##### 如果上述这些功能你恰好需要，那可以考虑这个基于spring boot2的无侵入式的boot start库依赖。    
另外这个工程可以带给你额外的便利:  
* 你仅需要提供一个请求对象，并定义一个处理该对象的类    
那么你就可以很方便的获得客服端发送过来的请求，并且该框架将会把这个对象转到你的消费类中进行处理，很方便。
                                                                                                                          
  
## 一种可能的应用场景示例
![PIC](https://github.com/zjtech/websocket-termination-spring-boot-starter/blob/master/sample.png)   

## 

## 如何开发
* 添加依赖    
对于gradle, 可以添加如下依赖
```   
compile "zjtech:websocket-termination-spring-boot-starter:0.1"
```   
* 在项目的配置文件中启用    
```
websocket:
  termination:
    scan:
      api-package: sample.api   

```
```api-package```指定了客户端与服务端WebSocket消息对象的存放路径

完整的配置项如下所示:   
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

                                                                                                                                  