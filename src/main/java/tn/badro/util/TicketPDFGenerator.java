package tn.badro.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import tn.badro.entities.Event;
import tn.badro.entities.Participation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TicketPDFGenerator {
    private static final String TICKETS_DIR = "tickets";

    static {
        // Ensure tickets directory exists
        try {
            Files.createDirectories(Paths.get(TICKETS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateTicket(Participation participation, Event event) throws IOException, WriterException {
        String ticketFileName = TICKETS_DIR + File.separator + "ticket_" + participation.getTicketCode() + ".pdf";
        
        // Create PDF document
        PdfWriter writer = new PdfWriter(new FileOutputStream(ticketFileName));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Add title
        Paragraph title = new Paragraph("Event Ticket");
        title.setFontSize(24);
        title.setBold();
        title.setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        
        // Create table for ticket details
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginTop(20);
        
        // Add event details to table
        addTableRow(table, "Event Name:", event.getTitre());
        addTableRow(table, "Date:", formatDate(event.getDate()));
        addTableRow(table, "Time:", event.getHeure() != null ? event.getHeure().toString() : "TBA");
        addTableRow(table, "Location:", event.getLieu() != null ? event.getLieu() : "TBA");
        addTableRow(table, "Participant:", participation.getNomParticipant());
        addTableRow(table, "Ticket Code:", participation.getTicketCode());
        
        document.add(table);
        
        // Add QR code
        document.add(new Paragraph("\n"));
        Paragraph qrTitle = new Paragraph("Scan QR Code to Verify Ticket");
        qrTitle.setTextAlignment(TextAlignment.CENTER);
        document.add(qrTitle);
        
        // Generate QR code data - include essential information
        String qrData = "TICKET:" + participation.getTicketCode() + 
                      ";EVENT:" + event.getTitre() + 
                      ";PARTICIPANT:" + participation.getNomParticipant() +
                      ";DATE:" + formatDate(event.getDate());
        
        // Generate QR code image
        byte[] qrCodeImage = generateQRCodeImage(qrData, 200, 200);
        
        // Create a temporary file for the QR code image
        Path tempQrPath = Files.createTempFile("qrcode_", ".png");
        Files.write(tempQrPath, qrCodeImage);
        
        // Add QR code image to document
        com.itextpdf.layout.element.Image qrCodeImg = new com.itextpdf.layout.element.Image(
                com.itextpdf.io.image.ImageDataFactory.create(tempQrPath.toUri().toURL()));
        qrCodeImg.setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(qrCodeImg);
        
        // Delete temporary file
        Files.deleteIfExists(tempQrPath);
        
        // Add footer
        Paragraph footer = new Paragraph("This ticket is valid for one person only. Please present this ticket at the event entrance.");
        footer.setFontSize(10);
        footer.setItalic();
        footer.setTextAlignment(TextAlignment.CENTER);
        footer.setMarginTop(20);
        document.add(footer);
        
        // Close document
        document.close();
        
        return ticketFileName;
    }
    
    private static void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold());
        labelCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        labelCell.setBackgroundColor(ColorConstants.LIGHT_GRAY, 0.2f);
        
        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value));
        valueCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1));
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private static byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
    
    private static String formatDate(java.time.LocalDate date) {
        if (date == null) return "TBA";
        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }
}