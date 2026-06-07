package com.pathshalapro.service.impl;

import com.pathshalapro.dto.fee.*;
import com.pathshalapro.entity.*;
import com.pathshalapro.entity.enums.NotificationType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fee Management Service - handles fee structures, invoices, and Razorpay integration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FeeServiceImpl {

    private final FeeStructureRepository feeStructureRepository;
    private final FeeHeadRepository feeHeadRepository;
    private final FeeGroupRepository feeGroupRepository;
    private final FeeAllocationRepository feeAllocationRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final NotificationServiceImpl notificationService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final ClassRoomRepository classRoomRepository;
    private final RazorpayClient razorpayClient;

    private final FeeInstallmentPlanRepository feeInstallmentPlanRepository;
    private final AdvanceCreditRepository advanceCreditRepository;
    private final StudentFeeConcessionRepository studentFeeConcessionRepository;
    private final LateFeeRuleRepository lateFeeRuleRepository;
    private final FeeAuditLogRepository feeAuditLogRepository;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    // Counter for invoice number generation
    private final AtomicLong invoiceCounter = new AtomicLong(1000);

    // ---- Fee Heads ----

    @Transactional
    public FeeHeadResponse createFeeHead(Long schoolId, FeeHeadRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        FeeHead head = FeeHead.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isMandatory(request.isMandatory())
                .school(school)
                .build();

        return mapToHeadResponse(feeHeadRepository.save(head));
    }

    @Transactional(readOnly = true)
    public List<FeeHeadResponse> getFeeHeads(Long schoolId) {
        return feeHeadRepository.findBySchoolIdAndIsDeletedFalse(schoolId)
                .stream().map(this::mapToHeadResponse).toList();
    }

    // ---- Fee Groups ----

    @Transactional
    public FeeGroupResponse createFeeGroup(Long schoolId, FeeGroupRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        FeeGroup group = FeeGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .grade(request.getGrade())
                .school(school)
                .build();

        List<FeeGroupItem> items = request.getItems().stream().map(itemReq -> {
            FeeHead head = feeHeadRepository.findById(itemReq.getFeeHeadId())
                    .orElseThrow(() -> ApiException.notFound("Fee head not found: " + itemReq.getFeeHeadId()));
            return FeeGroupItem.builder()
                    .feeGroup(group)
                    .feeHead(head)
                    .amount(itemReq.getAmount())
                    .build();
        }).toList();

        group.setFeeItems(new java.util.ArrayList<>(items));
        return mapToGroupResponse(feeGroupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<FeeGroupResponse> getFeeGroups(Long schoolId) {
        return feeGroupRepository.findBySchoolIdAndIsDeletedFalse(schoolId)
                .stream().map(this::mapToGroupResponse).toList();
    }

    // ---- Fee Allocations ----

    @Transactional
    public void createAllocation(Long schoolId, Long groupId, Long classId, Long studentId, String academicYear) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        FeeGroup group = feeGroupRepository.findById(groupId)
                .orElseThrow(() -> ApiException.notFound("Fee group not found."));

        FeeAllocation allocation = FeeAllocation.builder()
                .feeGroup(group)
                .academicYear(academicYear)
                .school(school)
                .build();

        if (classId != null) {
            ClassRoom classRoom = classRoomRepository.findById(classId)
                    .orElseThrow(() -> ApiException.notFound("Class not found."));
            allocation.setClassRoom(classRoom);
        }

        if (studentId != null) {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> ApiException.notFound("Student not found."));
            allocation.setStudent(student);
        }

        feeAllocationRepository.save(allocation);
    }

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

    @Transactional(readOnly = true)
    public List<FeeAllocationResponse> getAllocations(Long schoolId) {
        return feeAllocationRepository.findBySchoolIdAndIsDeletedFalse(schoolId)
                .stream()
                .map(this::mapToAllocationResponse)
                .toList();
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

    @Transactional
    public void generateInvoicesForClass(Long schoolId, Long classId, String academicYear, Integer month, Integer year, LocalDate dueDate) {
        ClassRoom classRoom = classRoomRepository.findById(classId)
                .orElseThrow(() -> ApiException.notFound("Class not found."));

        List<FeeAllocation> allocations = feeAllocationRepository
                .findByClassRoomIdAndAcademicYearAndIsDeletedFalse(classId, academicYear);

        if (allocations.isEmpty()) {
            throw ApiException.badRequest("No fee groups allocated to this class for the given academic year.");
        }

        for (User student : classRoom.getStudents()) {
            if (student.isDeleted()) continue;

            List<StudentFeeConcession> concessions = studentFeeConcessionRepository.findByStudentId(student.getId());

            for (FeeAllocation allocation : allocations) {
                FeeGroup group = allocation.getFeeGroup();
                
                // Check if invoice already exists
                boolean exists = feeInvoiceRepository.existsByStudentIdAndPeriodMonthAndPeriodYearAndAcademicYearAndIsDeletedFalse(
                        student.getId(), month, year, academicYear);
                
                if (exists) continue;

                BigDecimal totalAmount = group.getFeeItems().stream()
                        .map(FeeGroupItem::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Calculate Concession
                BigDecimal discount = BigDecimal.ZERO;
                for (StudentFeeConcession concession : concessions) {
                    if (concession.getFeeHead() == null) {
                        // Applies to total amount
                        if ("PERCENTAGE".equalsIgnoreCase(concession.getDiscountType())) {
                            discount = discount.add(totalAmount.multiply(concession.getValue()).divide(BigDecimal.valueOf(100)));
                        } else {
                            discount = discount.add(concession.getValue());
                        }
                    }
                }
                
                if (discount.compareTo(totalAmount) > 0) {
                    discount = totalAmount; // Cap discount
                }

                BigDecimal netAmount = totalAmount.subtract(discount);
                BigDecimal paidAmount = BigDecimal.ZERO;
                PaymentStatus paymentStatus = PaymentStatus.PENDING;

                // Consume Advance Credit
                AdvanceCredit advanceCredit = advanceCreditRepository.findByStudentId(student.getId()).orElse(null);
                if (advanceCredit != null && advanceCredit.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                    if (advanceCredit.getCreditAmount().compareTo(netAmount) >= 0) {
                        // Fully paid by advance
                        advanceCredit.setCreditAmount(advanceCredit.getCreditAmount().subtract(netAmount));
                        paidAmount = netAmount;
                        paymentStatus = PaymentStatus.PAID;
                    } else {
                        // Partially paid
                        paidAmount = advanceCredit.getCreditAmount();
                        advanceCredit.setCreditAmount(BigDecimal.ZERO);
                        paymentStatus = PaymentStatus.PARTIAL;
                    }
                    advanceCreditRepository.save(advanceCredit);
                }

                String invoiceNumber = generateInvoiceNumber(schoolId);

                FeeInvoice invoice = FeeInvoice.builder()
                        .invoiceNumber(invoiceNumber)
                        .totalAmount(totalAmount)
                        .discountAmount(discount)
                        .fineAmount(BigDecimal.ZERO)
                        .netAmount(netAmount)
                        .paidAmount(paidAmount)
                        .paymentStatus(paymentStatus)
                        .dueDate(dueDate)
                        .periodMonth(month)
                        .periodYear(year)
                        .academicYear(academicYear)
                        .school(classRoom.getSchool())
                        .student(student)
                        .remarks("Generated for " + group.getName())
                        .build();

                final FeeInvoice savedInvoice = feeInvoiceRepository.save(invoice);

                List<FeeInvoiceItem> items = group.getFeeItems().stream()
                        .map(item -> FeeInvoiceItem.builder()
                                .feeInvoice(savedInvoice)
                                .feeHead(item.getFeeHead())
                                .amount(item.getAmount())
                                .build())
                        .toList();
                
                savedInvoice.setItems(items);
                feeInvoiceRepository.save(savedInvoice);

                // Audit Log
                FeeAuditLog auditLog = FeeAuditLog.builder()
                        .entityType("INVOICE")
                        .entityId(savedInvoice.getId())
                        .action("CREATED")
                        .changedBy(1L) // System/Admin user
                        .school(classRoom.getSchool())
                        .newState("Invoice generated for " + netAmount)
                        .build();
                feeAuditLogRepository.save(auditLog);
            }
        }
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

    private FeeHeadResponse mapToHeadResponse(FeeHead h) {
        return FeeHeadResponse.builder()
                .id(h.getId())
                .name(h.getName())
                .description(h.getDescription())
                .isMandatory(h.isMandatory())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private FeeGroupResponse mapToGroupResponse(FeeGroup g) {
        return FeeGroupResponse.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .grade(g.getGrade())
                .items(g.getFeeItems().stream().map(i -> FeeGroupResponse.FeeGroupItemResponse.builder()
                        .id(i.getId())
                        .feeHeadId(i.getFeeHead().getId())
                        .feeHeadName(i.getFeeHead().getName())
                        .amount(i.getAmount())
                        .build()).toList())
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

    @Transactional
    public void notifyParentsOfPendingFees(Long schoolId, Long classId, String academicYear) {
        List<FeeInvoice> pendingInvoices = feeInvoiceRepository.findBySchoolIdAndAcademicYearAndIsDeletedFalse(schoolId, academicYear)
                .stream()
                .filter(i -> i.getPaymentStatus() != PaymentStatus.PAID)
                .filter(i -> classId == null || i.getStudent().getClassRoom().getId().equals(classId))
                .toList();

        for (FeeInvoice invoice : pendingInvoices) {
            User student = invoice.getStudent();
            User parent = student.getParent();

            String title = "Fee Payment Reminder: " + invoice.getInvoiceNumber();
            String message = String.format("Dear Parent, a fee of %s is pending for %s. Please pay online to avoid late fees.",
                    invoice.getNetAmount().toString(), student.getFirstName());

            // 1. In-app Notification to Parent
            if (parent != null) {
                notificationService.sendNotification(schoolId,
                        com.pathshalapro.dto.notification.NotificationRequest.builder()
                                .title(title)
                                .message(message)
                                .recipientId(parent.getId())
                                .notificationType(NotificationType.FEE_REMINDER)
                                .referenceId(invoice.getId())
                                .referenceType("FEE_INVOICE")
                                .build(),
                        null);
            }

            // 2. In-app Notification to Student
            notificationService.sendNotification(schoolId,
                    com.pathshalapro.dto.notification.NotificationRequest.builder()
                            .title(title)
                            .message(message)
                            .recipientId(student.getId())
                            .notificationType(NotificationType.FEE_REMINDER)
                            .referenceId(invoice.getId())
                            .referenceType("FEE_INVOICE")
                            .build(),
                    null);

            log.info("Notification sent for invoice: {}", invoice.getInvoiceNumber());
        }
    }

    private FeeAllocationResponse mapToAllocationResponse(FeeAllocation a) {
        FeeAllocationResponse.FeeAllocationResponseBuilder builder = FeeAllocationResponse.builder()
                .id(a.getId())
                .groupId(a.getFeeGroup().getId())
                .groupName(a.getFeeGroup().getName())
                .academicYear(a.getAcademicYear())
                .createdAt(a.getCreatedAt());

        if (a.getClassRoom() != null) {
            builder.classId(a.getClassRoom().getId())
                    .className(a.getClassRoom().getName())
                    .section(a.getClassRoom().getSection());
        }

        if (a.getStudent() != null) {
            builder.studentId(a.getStudent().getId())
                    .studentName(a.getStudent().getFirstName() + " " + a.getStudent().getLastName());
        }

        return builder.build();
    }
}
