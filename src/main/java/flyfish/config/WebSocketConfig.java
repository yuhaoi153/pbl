package flyfish.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user"); // 启用内存消息代理
        config.setApplicationDestinationPrefixes("/app"); //
        config.setUserDestinationPrefix("/user"); //
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 对应前端 WS_URL，这里仅用于升级http请求
                .setAllowedOriginPatterns("*"); // 允许跨域
//                .withSockJS();
    }


    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(100 * 1024 * 1024); // 消息大小限制
        registration.setSendTimeLimit(30 * 1000);     // 发送超时时间
    }

}


