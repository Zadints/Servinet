package org.example.servinet.core.application.usecase.api;

import org.example.servinet.core.application.dto.DniDataDto;
import org.example.servinet.core.domain.repository.WhatsAppSendProvider;
import org.example.servinet.infrastructure.database.config.ConfigLoad;

public class WhatsAppUseCase {
    private final WhatsAppSendProvider whatsAppSendProvider;

    public WhatsAppUseCase(WhatsAppSendProvider whatsAppSendProvider) {
        this.whatsAppSendProvider = whatsAppSendProvider;
    }

    public void sendMessage(String phone, String message) {
        whatsAppSendProvider.sendMessage(phone, message, ConfigLoad.getYmlWhatsApp());
    }


    /*
    ¿Cómo uso esto?


    try {
        WhatsAppUseCase test = new WhatsAppUseCase(new SendWhatsAppMessageUseCase());
        test.sendMessage("120363305525800742@g.us","""
            🤖 *SERVINET BOT*

            👋 ¡Hola! Esta es una prueba de nuestro asistente automático.

            ✅ Conexión con WhatsApp establecida correctamente.

            📡 *Estado:* Operativo
            ⚡ *Servicio:* Servinet
            🧪 *Modo:* Prueba

            Gracias por probar el bot. 🚀
            """
        );
    } catch(ApiException e){
        System.out.println(e.getMessage());
    }

    */
}
