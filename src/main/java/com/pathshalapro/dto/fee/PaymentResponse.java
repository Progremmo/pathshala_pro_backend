package com.pathshalapro.dto.fee;

import com.pathshalapro.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String receiptNumber;
    private String notes;
    private Long invoiceId;
    private Long paidById;
    private String paidByName;
}
