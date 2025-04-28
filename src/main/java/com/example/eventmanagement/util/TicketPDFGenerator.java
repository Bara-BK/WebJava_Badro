package com.example.eventmanagement.util;

import com.example.eventmanagement.entity.Event;
import com.example.eventmanagement.entity.Participation;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.image.ImageDataFactory;

import java.io.ByteArrayOutputStream;
import java.net.URL;

public class TicketPDFGenerator {
    public static String generateTicketPDF(Participation participation, Event event) throws Exception {
        String participationId = participation.getParticipantId().toString();
        String homeDir = System.getProperty("user.home");
        String dest = homeDir + "/Downloads/ticket_" + participationId + ".pdf";

        // Initialize PDF
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add background
        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());
        Rectangle pageSize = pdf.getDefaultPageSize();

        // Light blue gradient (CornflowerBlue to LightBlue)
        for (int i = 0; i < 50; i++) {
            float t = i / 49f;
            int r = (int) (100 + (173 - 100) * t); // Red: 100 to 173
            int g = (int) (149 + (216 - 149) * t); // Green: 149 to 216
            int b = (int) (237 + (230 - 237) * t); // Blue: 237 to 230
            canvas.saveState()
                  .setFillColor(new DeviceRgb(r, g, b))
                  .rectangle(0, pageSize.getHeight() * (1 - i / 50f), pageSize.getWidth(), pageSize.getHeight() / 50)
                  .fill()
                  .restoreState();
        }

        // Decorative border
        canvas.setStrokeColor(ColorConstants.BLACK)
              .setLineWidth(2)
              .rectangle(20, 20, pageSize.getWidth() - 40, pageSize.getHeight() - 40)
              .stroke();

        // Subtle horizontal line separator
        canvas.setStrokeColor(ColorConstants.DARK_GRAY)
              .setLineWidth(1)
              .moveTo(50, pageSize.getHeight() - 100)
              .lineTo(pageSize.getWidth() - 50, pageSize.getHeight() - 100)
              .stroke();

        // Logo at top right
        String logoPath = "images/logo.png"; // Relative to src/main/resources
        URL logoUrl = TicketPDFGenerator.class.getClassLoader().getResource(logoPath);
        if (logoUrl == null) {
            System.err.println("Warning: Logo image not found at: " + logoPath);
        } else {
            Image logoImage = new Image(ImageDataFactory.create(logoUrl))
                .setFixedPosition(pageSize.getWidth() - 120, pageSize.getHeight() - 80)
                .setWidth(100)
                .setHeight(50);
            document.add(logoImage);
        }

        // Ticket content
        Text titleText = new Text("Event: " + event.getTitre())
            .setFontSize(20)
            .setBold()
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.WHITE, 0.8f);
        Paragraph title = new Paragraph(titleText)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30);
        document.add(title);

        Text participantText = new Text("Participant: " + participation.getNomParticipant())
            .setFontSize(14)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.WHITE, 0.8f);
        Paragraph participant = new Paragraph(participantText)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10);
        document.add(participant);

        Text locationText = new Text("Location: " + (event.getLieu() != null ? event.getLieu() : "N/A"))
            .setFontSize(12)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.WHITE, 0.8f);
        Paragraph location = new Paragraph(locationText)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10);
        document.add(location);

        Text dateText = new Text("Date: " + (event.getDate() != null ? event.getDate().toString() : "N/A"))
            .setFontSize(12)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.WHITE, 0.8f);
        Paragraph date = new Paragraph(dateText)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10);
        document.add(date);

        Text timeText = new Text("Time: " + (event.getHeure() != null ? event.getHeure().toString() : "N/A"))
            .setFontSize(12)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.WHITE, 0.8f);
        Paragraph time = new Paragraph(timeText)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10);
        document.add(time);

        // QR code
        String ticketCode = participation.getTicketCode() != null ? participation.getTicketCode() : "N/A";
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(ticketCode, BarcodeFormat.QR_CODE, 150, 150);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        Image qrImage = new Image(ImageDataFactory.create(pngData))
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
            .setMarginTop(20);
        document.add(qrImage);

        Text ticketCodeText = new Text("Ticket Code: " + ticketCode)
            .setFontSize(12)
            .setFontColor(ColorConstants.BLACK)
            .setBackgroundColor(ColorConstants.WHITE, 0.8f);
        Paragraph ticketCodePara = new Paragraph(ticketCodeText)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10);
        document.add(ticketCodePara);

        // Footer
        Text footerText = new Text("Thank you for attending!")
            .setFontSize(10)
            .setItalic()
            .setFontColor(ColorConstants.DARK_GRAY)
            .setBackgroundColor(ColorConstants.WHITE, 0.8f);
        Paragraph footer = new Paragraph(footerText)
            .setTextAlignment(TextAlignment.CENTER)
            .setFixedPosition(50, 80, pageSize.getWidth() - 100)
            .setVerticalAlignment(VerticalAlignment.BOTTOM);
        document.add(footer);

        // Ticket image at bottom
        String ticketPath = "images/ticket.png"; 
        URL ticketUrl = TicketPDFGenerator.class.getClassLoader().getResource(ticketPath);
        if (ticketUrl == null) {
            System.err.println("Warning: Ticket image not found at: " + ticketPath);
        } else {
            Image ticketImage = new Image(ImageDataFactory.create(ticketUrl))
                .setFixedPosition(50, 30)
                .setWidth(100)
                .setHeight(50);
            document.add(ticketImage);
        }

        document.close();
        return dest;
    }
}