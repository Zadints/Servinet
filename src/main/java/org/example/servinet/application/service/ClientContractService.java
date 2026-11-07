package org.example.servinet.application.service;

import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.port.out.PdfProvider;

import java.io.IOException;

public class ClientContractService {
    private final PdfProvider pdfGenerator;
    private final WhatsAppService whatsAppService;
    private final GmailService gmailService;

    public ClientContractService(PdfProvider pdfGenerator, WhatsAppService whatsAppService, GmailService gmailService) {
        this.pdfGenerator = pdfGenerator;
        this.whatsAppService = whatsAppService;
        this.gmailService = gmailService;
    }

    public void sendMailClientContract(ClientDto dto) {

        final byte[] pdf;

        try {
            pdf = pdfGenerator.generate(dto);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo generar el contrato PDF", e);
        }

        String filename = "Contrato_Servinet_" + dto.dni() + ".pdf";

        try {
            gmailService.sendEmail(
                    dto.email(),
                    "Contrato de servicio - Servinet",
                    "Hola " + dto.firstName() + ",\n\n" +
                            "Adjuntamos tu contrato de contratación de Servinet.\n\n" +
                            "Gracias por confiar en nosotros.",
                    pdf,
                    filename
            );
        }catch (Exception e){
            System.out.println(e.getMessage() + e.getCause());
        }

        try {
            if (dto.whatsapp() != null && !dto.whatsapp().isBlank()) {
                whatsAppService.sendDocument(
                        dto.whatsapp(),
                        pdf,
                        filename,
                        "Hola " + dto.firstName() +
                                ", te compartimos tu contrato de contratación de Servinet."
                );
            }
        }catch (Exception e){
            System.out.println(e.getMessage() + e.getCause());
        }

    }
}
