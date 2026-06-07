package com.pathshalapro.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.pathshalapro.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    public byte[] generatePaymentReceipt(Payment payment) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Fee Payment Receipt", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // School Info
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            
            document.add(new Paragraph("School Name: " + payment.getSchool().getName(), boldFont));
            document.add(new Paragraph("Receipt No: " + payment.getReceiptNumber(), normalFont));
            document.add(new Paragraph("Date: " + payment.getPaymentDate(), normalFont));
            document.add(new Paragraph("Transaction ID: " + payment.getRazorpayPaymentId(), normalFont));
            document.add(Chunk.NEWLINE);

            // Student Info
            document.add(new Paragraph("Student Name: " + payment.getPaidBy().getFirstName() + " " + payment.getPaidBy().getLastName(), normalFont));
            document.add(new Paragraph("Invoice No: " + payment.getFeeInvoice().getInvoiceNumber(), normalFont));
            document.add(Chunk.NEWLINE);

            // Payment Details Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            PdfPCell cell = new PdfPCell(new Phrase("Description", boldFont));
            cell.setPadding(5);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Amount (" + payment.getCurrency() + ")", boldFont));
            cell.setPadding(5);
            table.addCell(cell);

            table.addCell("Amount Paid");
            table.addCell(payment.getAmount().toString());

            document.add(table);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Thank you for the payment.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Failed to generate PDF receipt", e);
            throw new RuntimeException("Error generating receipt PDF");
        }
    }
}
