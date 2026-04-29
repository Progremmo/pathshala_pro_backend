package com.pathshalapro.service.impl;

import com.pathshalapro.dto.fee.*;
import com.pathshalapro.entity.*;
import com.pathshalapro.entity.enums.PaymentStatus;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fee Management Service - handles fee structures, invoices, and Razorpay integration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeeServiceImpl {

    private final FeeStructureRepository feeStructureRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    // Counter for invoice number generation
    private final AtomicLong invoiceCounter = new AtomicLong(1000);

    // ---- Fee Structure ----

    @Transactional
    public FeeStructureResponse createFeeStructure(Long schoolId, FeeStructureRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found: " + schoolId));

        FeeStructure structure = FeeStructure.builder()
                .name(request.getName())
                .feeType(request.getFeeType())
                .amount(request.getAmount())
                .frequency(request.getFrequency())
                .grade(request.getGrade())
                .academicYear(request.getAcademicYear())
                .description(request.getDescription())
                .dueDay(request.getDueDay())
                .school(school)
                .build();

        return mapToStructureResponse(feeStructureRepository.save(structure));
    }

    @Transactional(readOnly = true)
    public Page<FeeStructureResponse> getFeeStructures(Long schoolId, Pageable pageable) {
        return feeStructureRepository.findBySchoolIdAndIsDeletedFalse(schoolId, pageable)
                .map(this::mapToStructureResponse);
    }

    @Transactional
    public FeeStructureResponse updateFeeStructure(Long schoolId, Long structureId, FeeStructureRequest request) {
        FeeStructure structure = feeStructureRepository.findById(structureId)
                .filter(s -> s.getSchool().getId().equals(schoolId) && !s.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Fee structure not found."));

        structure.setName(request.getName());
        structure.setFeeType(request.getFeeType());
        structure.setAmount(request.getAmount());
        structure.setFrequency(request.getFrequency());
        structure.setGrade(request.getGrade());
        structure.setAcademicYear(request.getAcademicYear());
        structure.setDescription(request.getDescription());
        structure.setDueDay(request.getDueDay());

        return mapToStructureResponse(feeStructureRepository.save(structure));
    }

    @Transactional
    public void deleteFeeStructure(Long schoolId, Long structureId) {
        FeeStructure structure = feeStructureRepository.findById(structureId)
                .filter(s -> s.getSchool().getId().equals(schoolId) && !s.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Fee structure not found."));

        structure.setDeleted(true);
        feeStructureRepository.save(structure);
    }

    // ---- Fee Invoice ----

    @Transactional
    public FeeInvoiceResponse createInvoice(Long schoolId, FeeInvoiceRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        User student = userRepository.findByIdAndIsDeletedFalse(request.getStudentId())
                .orElseThrow(() -> ApiException.notFound("Student not found: " + request.getStudentId()));

        FeeStructure structure = feeStructureRepository.findById(request.getFeeStructureId())
                .orElseThrow(() -> ApiException.notFound("Fee structure not found."));

        // Prevent duplicate invoice for same period
        if (request.getPeriodMonth() != null && request.getPeriodYear() != null) {
            boolean exists = feeInvoiceRepository
                    .existsByStudentIdAndFeeStructureIdAndPeriodMonthAndPeriodYearAndIsDeletedFalse(
                            request.getStudentId(), request.getFeeStructureId(),
                            request.getPeriodMonth(), request.getPeriodYear());
            if (exists) {
                throw ApiException.conflict("Invoice already exists for this student and period.");
            }
        }

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal fine = request.getFineAmount() != null ? request.getFineAmount() : BigDecimal.ZERO;
        BigDecimal netAmount = request.getTotalAmount().subtract(discount).add(fine);

        String invoiceNumber = generateInvoiceNumber(schoolId);

        FeeInvoice invoice = FeeInvoice.builder()
                .invoiceNumber(invoiceNumber)
                .totalAmount(request.getTotalAmount())
                .discountAmount(discount)
                .fineAmount(fine)
                .netAmount(netAmount)
                .paidAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PENDING)
                .dueDate(request.getDueDate())
                .periodMonth(request.getPeriodMonth())
                .periodYear(request.getPeriodYear())
                .academicYear(request.getAcademicYear())
                .remarks(request.getRemarks())
                .school(school)
                .student(student)
                .feeStructure(structure)
                .build();

        return mapToResponse(feeInvoiceRepository.save(invoice));
    }

    @Transactional(readOnly = true)
    public Page<FeeInvoiceResponse> getInvoicesBySchool(Long schoolId, Pageable pageable) {
        return feeInvoiceRepository.findBySchoolIdAndIsDeletedFalse(schoolId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<FeeInvoiceResponse> getInvoicesByStudent(Long studentId, Pageable pageable) {
        return feeInvoiceRepository.findByStudentIdAndIsDeletedFalse(studentId, pageable).map(this::mapToResponse);
    }

    @Transactional
    public FeeInvoiceResponse updateInvoice(Long schoolId, Long invoiceId, FeeInvoiceRequest request) {
        FeeInvoice invoice = feeInvoiceRepository.findById(invoiceId)
                .filter(i -> i.getSchool().getId().equals(schoolId) && !i.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Invoice not found."));

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw ApiException.badRequest("Cannot update a paid invoice.");
        }

        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setDiscountAmount(request.getDiscountAmount());
        invoice.setFineAmount(request.getFineAmount());
        invoice.setNetAmount(request.getTotalAmount().subtract(request.getDiscountAmount()).add(request.getFineAmount()));
        invoice.setDueDate(request.getDueDate());
        invoice.setRemarks(request.getRemarks());

        return mapToResponse(feeInvoiceRepository.save(invoice));
    }

    @Transactional
    public void deleteInvoice(Long schoolId, Long invoiceId) {
        FeeInvoice invoice = feeInvoiceRepository.findById(invoiceId)
                .filter(i -> i.getSchool().getId().equals(schoolId) && !i.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Invoice not found."));

        if (invoice.getPaymentStatus() != PaymentStatus.PENDING) {
            throw ApiException.badRequest("Only pending invoices can be deleted.");
        }

        invoice.setDeleted(true);
        feeInvoiceRepository.save(invoice);
    }

    // ---- Razorpay Integration ----

    /**
     * Step 1: Create a Razorpay order for a fee invoice.
     * Returns the order details needed by the frontend Razorpay SDK.
     */
    @Transactional
    public Map<String, Object> createRazorpayOrder(Long schoolId, RazorpayOrderRequest request) {
        FeeInvoice invoice = feeInvoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> ApiException.notFound("Invoice not found."));

        if (!invoice.getSchool().getId().equals(schoolId)) {
            throw ApiException.forbidden("Access denied.");
        }

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw ApiException.badRequest("Invoice is already paid.");
        }

        try {
            // Amount in paise (1 INR = 100 paise)
            int amountInPaise = request.getAmount().multiply(BigDecimal.valueOf(100)).intValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "INV-" + invoice.getInvoiceNumber());
            orderRequest.put("notes", new JSONObject()
                    .put("invoiceId", invoice.getId())
                    .put("studentId", invoice.getStudent().getId())
                    .put("schoolId", schoolId));

            Order order = razorpayClient.orders.create(orderRequest);

            // Create pending payment record
            Payment payment = Payment.builder()
                    .amount(request.getAmount())
                    .currency(currency)
                    .status(PaymentStatus.PENDING)
                    .razorpayOrderId(order.get("id"))
                    .receiptNumber("REC-" + invoice.getInvoiceNumber())
                    .notes(request.getNotes())
                    .school(invoice.getSchool())
                    .feeInvoice(invoice)
                    .paidBy(invoice.getStudent())
                    .build();

            paymentRepository.save(payment);

            log.info("Razorpay order created: {} for invoice: {}", order.get("id"), invoice.getInvoiceNumber());

            return Map.of(
                    "orderId", (String) order.get("id"),
                    "amount", amountInPaise,
                    "currency", currency,
                    "invoiceId", invoice.getId(),
                    "invoiceNumber", invoice.getInvoiceNumber()
            );
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Payment gateway error: " + e.getMessage());
        }
    }

    /**
     * Step 2: Verify Razorpay payment signature and update payment status.
     */
    @Transactional
    public PaymentResponse verifyPayment(PaymentVerifyRequest request) {
        // Find existing payment attempt
        Payment payment = paymentRepository.findByRazorpayOrderIdAndIsDeletedFalse(request.getRazorpayOrderId())
                .orElseThrow(() -> ApiException.notFound("Payment order not found."));

        // Verify signature
        if (!verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid signature");
            paymentRepository.save(payment);
            throw ApiException.badRequest("Payment verification failed: Invalid signature.");
        }

        // Update payment
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update invoice
        FeeInvoice invoice = payment.getFeeInvoice();
        invoice.setPaidAmount(invoice.getNetAmount()); // Full payment for now
        invoice.setPaymentStatus(PaymentStatus.PAID);
        feeInvoiceRepository.save(invoice);

        return mapToPaymentResponse(payment);
    }

    /**
     * Reports: Get total collected amount for a school in a year.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalCollected(Long schoolId, Integer year) {
        BigDecimal amount = feeInvoiceRepository.getTotalCollectedBySchoolAndYear(schoolId, year);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstanding(Long schoolId) {
        BigDecimal amount = feeInvoiceRepository.getTotalOutstandingBySchool(schoolId);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    // ---- Helpers ----

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(razorpaySecret.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes());
            String expectedSignature = HexFormat.of().formatHex(hash);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String generateInvoiceNumber(Long schoolId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("INV-%d-%s-%04d", schoolId, timestamp, invoiceCounter.getAndIncrement());
    }

    private FeeStructureResponse mapToStructureResponse(FeeStructure s) {
        return FeeStructureResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .feeType(s.getFeeType())
                .amount(s.getAmount())
                .frequency(s.getFrequency())
                .grade(s.getGrade())
                .academicYear(s.getAcademicYear())
                .description(s.getDescription())
                .dueDay(s.getDueDay())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .paymentMethod(p.getPaymentMethod())
                .paymentDate(p.getPaymentDate())
                .receiptNumber(p.getReceiptNumber())
                .notes(p.getNotes())
                .invoiceId(p.getFeeInvoice().getId())
                .paidById(p.getPaidBy().getId())
                .paidByName(p.getPaidBy().getFirstName() + " " + p.getPaidBy().getLastName())
                .build();
    }

    private FeeInvoiceResponse mapToResponse(FeeInvoice i) {
        return FeeInvoiceResponse.builder()
                .id(i.getId())
                .invoiceNumber(i.getInvoiceNumber())
                .totalAmount(i.getTotalAmount())
                .discountAmount(i.getDiscountAmount())
                .fineAmount(i.getFineAmount())
                .netAmount(i.getNetAmount())
                .paidAmount(i.getPaidAmount())
                .paymentStatus(i.getPaymentStatus())
                .dueDate(i.getDueDate())
                .periodMonth(i.getPeriodMonth())
                .periodYear(i.getPeriodYear())
                .academicYear(i.getAcademicYear())
                .remarks(i.getRemarks())
                .studentId(i.getStudent().getId())
                .studentName(i.getStudent().getFirstName() + " " + i.getStudent().getLastName())
                .admissionNumber(i.getStudent().getAdmissionNo())
                .feeStructureId(i.getFeeStructure().getId())
                .feeStructureName(i.getFeeStructure().getName())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
