package com.pathshalapro.dto.fee;

import com.pathshalapro.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeInvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal fineAmount;
    private BigDecimal netAmount;
    private BigDecimal paidAmount;
    private PaymentStatus paymentStatus;
    private LocalDate dueDate;
    private Integer periodMonth;
    private Integer periodYear;
    private String academicYear;
    private String remarks;
    
    private Long studentId;
    private String studentName;
    private String admissionNumber;
    
    private Long feeStructureId;
    private String feeStructureName;
    
    private LocalDateTime createdAt;
}
