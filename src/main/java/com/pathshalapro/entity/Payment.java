package com.pathshalapro.entity;

import com.pathshalapro.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment record linked to Razorpay.
 * Stores payment attempt details and Razorpay transaction info.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_school", columnList = "school_id"),
    @Index(name = "idx_payment_invoice", columnList = "fee_invoice_id"),
    @Index(name = "idx_payment_razorpay_order", columnList = "razorpay_order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 5)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    // Razorpay fields
    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 500)
    private String razorpaySignature;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // UPI, CARD, NETBANKING, etc.

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "receipt_number", length = 100)
    private String receiptNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_invoice_id", nullable = false)
    private FeeInvoice feeInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;
}
