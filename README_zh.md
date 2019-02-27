== Websocket终结

如果你的应用是一个websocket的服务端，你需要在收到一个websocket消息时，将该消息传递到后端的服务。可是很不幸的是后端的服务提供的是Restful API, 此时你需要在websocket 服务端先将websocket终结掉，然后将请求转发到后端的Restful服务。此时你可以使用这个项目。

这个starter是否适合我的项目？
你可以考虑是否需要这些功能：
* 这个项目是基于Spring boot2和webflux的实现
* 我需要在websocket服务端收到请求后随后调用后端的服务并返回处理结果
* 后端的Restful服务可以调用websocket服务端的actuator endpoint， 基于Restful API的方式反向下发通知给websocket客户端。
* websocket服务端提供PING/PONG机制，当客户端连接后通过PING/PONG维持心跳                                                                                                                           
                                                                                                                                  