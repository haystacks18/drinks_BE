package com.goormfj.hanzan.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class SpringConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    // WebSocketHandler 에 관한 생성자 추가
    public SpringConfig(@Qualifier("webSocketChatHandler") WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // endpoint 설정 : /ws/chat
        // 이를 통해서 ws://localhost:8080/ws/chat 으로 요청이 들어오면 websocket 통신을 진행한다.
        registry.addHandler(webSocketHandler, "/ws/chat").setAllowedOriginPatterns("*");
    }
}
