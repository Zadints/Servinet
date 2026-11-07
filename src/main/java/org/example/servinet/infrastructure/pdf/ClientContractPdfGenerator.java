package org.example.servinet.infrastructure.pdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.servinet.application.dto.ClientDto;
import org.example.servinet.application.port.out.PdfProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ClientContractPdfGenerator implements PdfProvider {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 10;
    private static final float LINE_HEIGHT = 16;

    private static final PDType1Font FONT =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA);

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generate(ClientDto dto) throws IOException {

        if (dto == null) {
            throw new IllegalArgumentException("Los datos del cliente son obligatorios");
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDFWriter writer = new PDFWriter(document);

            writer.title("SERVINET");
            writer.subtitle("FICHA DE CONTRATACION DE SERVICIO");

            writer.paragraph(
                    "Fecha de emision: " +
                            LocalDateTime.now().format(DATE_FORMAT)
            );

            writer.section("1. DATOS DEL CLIENTE");

            writer.field("DNI", dto.dni());
            writer.field("Nombres", dto.firstName());
            writer.field("Apellidos", dto.lastName());
            writer.field("Telefono", dto.phone());
            writer.field("WhatsApp", dto.whatsapp());
            writer.field("Correo electronico", dto.email());
            writer.field("Direccion", dto.address());

            writer.section("2. PLAN CONTRATADO");

            if (dto.plan() != null) {
                writer.field(
                        "Precio mensual",
                        String.format(Locale.US, "S/ %.2f", dto.plan().getPrice())
                );
            } else {
                writer.field("Plan", "No especificado");
            }

            writer.section("3. DATOS DE INSTALACION");

            writer.field("ID de antena", dto.antennaUuid());
            writer.field("Latitud", formatCoordinate(dto.latitude()));
            writer.field("Longitud", formatCoordinate(dto.longitude()));

            writer.section("4. CONFORMIDAD");

            writer.paragraph(
                    "El cliente declara que los datos proporcionados " +
                            "son correctos y solicita la contratacion del servicio " +
                            "descrito en este documento."
            );

            writer.blank();

            writer.paragraph("Firma del cliente:");

            writer.blank();
            writer.blank();
            writer.paragraph("________________________________");
            writer.paragraph(
                    dto.firstName() + " " + dto.lastName()
            );
            writer.paragraph("DNI: " + dto.dni());

            writer.footer("SERVINET - Documento de contratacion");

            document.save(output);

            return output.toByteArray();
        }
    }

    private static String formatCoordinate(Double value) {
        return value != null
                ? String.format(Locale.US, "%.7f", value)
                : "No registrada";
    }

    private static class PDFWriter {

        private final PDDocument document;
        private PDPageContentStream content;
        private float y;

        private final float pageWidth = PDRectangle.A4.getWidth();
        private final float pageHeight = PDRectangle.A4.getHeight();

        PDFWriter(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        private void newPage() throws IOException {

            if (content != null) {
                content.close();
            }

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            content = new PDPageContentStream(document, page);
            y = pageHeight - MARGIN;
        }

        void title(String value) throws IOException {
            write(value, FONT_BOLD, 22);
            y -= 8;
        }

        void subtitle(String value) throws IOException {
            write(value, FONT_BOLD, 13);
            y -= 12;
        }

        void section(String value) throws IOException {
            checkSpace(35);

            y -= 10;
            write(value, FONT_BOLD, 12);
            y -= 5;
        }

        void field(String label, String value) throws IOException {
            paragraph(label + ": " + safe(value));
        }

        void paragraph(String value) throws IOException {

            String[] words = value.split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String word : words) {

                String candidate = line.isEmpty()
                        ? word
                        : line + " " + word;

                float width = FONT.getStringWidth(candidate) / 1000 * FONT_SIZE;

                if (width > pageWidth - (MARGIN * 2)) {
                    write(line.toString(), FONT, FONT_SIZE);
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }

            if (!line.isEmpty()) {
                write(line.toString(), FONT, FONT_SIZE);
            }
        }

        void blank() throws IOException {
            y -= LINE_HEIGHT;
            checkSpace(20);
        }

        void footer(String value) throws IOException {
            content.beginText();
            content.setFont(FONT, 8);
            content.newLineAtOffset(MARGIN, 25);
            content.showText(value);
            content.endText();

            content.close();
        }

        private void write(
                String value,
                PDType1Font font,
                float size
        ) throws IOException {

            checkSpace(LINE_HEIGHT);

            content.beginText();
            content.setFont(font, size);
            content.newLineAtOffset(MARGIN, y);
            content.showText(safe(value));
            content.endText();

            y -= LINE_HEIGHT;
        }

        private void checkSpace(float required) throws IOException {
            if (y - required < MARGIN + 30) {
                newPage();
            }
        }

        private String safe(String value) {
            if (value == null || value.isBlank()) {
                return "No especificado";
            }

            // Helvetica Standard-14 utiliza codificacion WinAnsi.
            return value.replaceAll("[^\\x20-\\x7E\\u00A0-\\u00FF]", "?");
        }
    }
}
