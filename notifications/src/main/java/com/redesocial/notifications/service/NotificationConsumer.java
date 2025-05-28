package com.redesocial.notifications.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationConsumer {

    private final WebSocketHandler webSocketHandler;

    public NotificationConsumer(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @KafkaListener(topics = "notificacoes", groupId = "notificacoes-group")
    public void consumirNotificacao(ConsumerRecord<String, String> record) throws IOException {
        String mensagem = "🔔 Nova Notificação: " + record.value();
        System.out.println(mensagem);

        webSocketHandler.enviarNotificacaoParaUsuario(record.key(), record.value());
    }
}