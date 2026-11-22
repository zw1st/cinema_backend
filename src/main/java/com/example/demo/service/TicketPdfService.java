package com.example.demo.service;

import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.example.demo.entity.OrderEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.TicketRepository;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.image.BufferedImage;

@Service
public class TicketPdfService {

    private final TicketRepository ticketRepository;

    public TicketPdfService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public byte[] generateTicketPdf(Long ticketId, Long userId) {

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));

        OrderEntity order = ticket.getOrder();

        // ownership check
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, outputStream);

            document.open();

            // =========================
            // Header
            // =========================

            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD);

            Paragraph title = new Paragraph(
                    "ABSOLUTE CINEMA",
                    titleFont);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("MOVIE TICKET"));
            document.add(new Paragraph(" "));

            // =========================
            // Ticket info
            // =========================

            document.add(new Paragraph("Order ID: " + order.getId()));
            document.add(new Paragraph("Ticket ID: " + ticket.getId()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Movie session"));

            document.add(new Paragraph(
                    "Movie title: " + ticket.getSession().getMovie().getTitle()));

            document.add(new Paragraph(
                    "Date: " + ticket.getSession().getDate()));

            document.add(new Paragraph(
                    "Start time: " + ticket.getSession().getStartTime()));

            document.add(new Paragraph(
                    "End time: " + ticket.getSession().getEndTime()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Seat"));

            document.add(new Paragraph("Row: " + ticket.getRowNum()));
            document.add(new Paragraph("Seat: " + ticket.getColNum()));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Status: " + ticket.getStatus()));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(" "));

            // =========================
            // QR generation
            // =========================

            Image qrImage = generateQrCode(ticket.getTicketCode());

            qrImage.scaleToFit(200, 200);

            qrImage.setAlignment(Element.ALIGN_CENTER);

            document.add(qrImage);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate pdf", e);
        }
    }

    private Image generateQrCode(String data) throws Exception {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        var bitMatrix = qrCodeWriter.encode(
                data,
                BarcodeFormat.QR_CODE,
                300,
                300);

        BufferedImage bufferedImage = new BufferedImage(
                300,
                300,
                BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {

                bufferedImage.setRGB(
                        x,
                        y,
                        bitMatrix.get(x, y)
                                ? java.awt.Color.BLACK.getRGB()
                                : java.awt.Color.WHITE.getRGB());
            }
        }

        ByteArrayOutputStream pngOutput = new ByteArrayOutputStream();

        ImageIO.write(bufferedImage, "png", pngOutput);

        return Image.getInstance(pngOutput.toByteArray());
    }

}
