package com.redesocial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void enviarNotificacao(String mensagem, String userId) {
        kafkaTemplate.send("notificacoes", userId, mensagem);
        System.out.println("Notificação enviada: " + mensagem);
    }
}
