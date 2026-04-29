package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.fee.*;
import com.pathshalapro.service.impl.FeeServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Fee management and Razorpay payment controller.
 */
@RestController
@RequestMapping("/schools/{schoolId}/fees")
@RequiredArgsConstructor
@Tag(name = "Fee Management", description = "Manage fee structures, invoices, and Razorpay payments")
public class FeeController {

    private final FeeServiceImpl feeService;

    // ---- Fee Structures ----

    @PostMapping("/structures")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Create fee structure", description = "Define a new fee structure for a class/grade.")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> createFeeStructure(
            @PathVariable Long schoolId,
            @Valid @RequestBody FeeStructureRequest request) {
        FeeStructureResponse structure = feeService.createFeeStructure(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(structure, "Fee structure created."));
    }

    @GetMapping("/structures")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Get all fee structures for a school")
    public ResponseEntity<ApiResponse<Page<FeeStructureResponse>>> getFeeStructures(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(feeService.getFeeStructures(schoolId, pageable)));
    }

    @PutMapping("/structures/{structureId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update fee structure")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> updateFeeStructure(
            @PathVariable Long schoolId,
            @PathVariable Long structureId,
            @Valid @RequestBody FeeStructureRequest request) {
        return ResponseEntity.ok(ApiResponse.success(feeService.updateFeeStructure(schoolId, structureId, request)));
    }

    @DeleteMapping("/structures/{structureId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Delete fee structure")
    public ResponseEntity<ApiResponse<Void>> deleteFeeStructure(
            @PathVariable Long schoolId,
            @PathVariable Long structureId) {
        feeService.deleteFeeStructure(schoolId, structureId);
        return ResponseEntity.ok(ApiResponse.success(null, "Fee structure deleted."));
    }

    // ---- Fee Invoices ----

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Create fee invoice for a student")
    public ResponseEntity<ApiResponse<FeeInvoiceResponse>> createInvoice(
            @PathVariable Long schoolId,
            @Valid @RequestBody FeeInvoiceRequest request) {
        FeeInvoiceResponse invoice = feeService.createInvoice(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(invoice, "Invoice created."));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Get all invoices for a school")
    public ResponseEntity<ApiResponse<Page<FeeInvoiceResponse>>> getInvoicesBySchool(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(feeService.getInvoicesBySchool(schoolId, pageable)));
    }

    @PutMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Update invoice")
    public ResponseEntity<ApiResponse<FeeInvoiceResponse>> updateInvoice(
            @PathVariable Long schoolId,
            @PathVariable Long invoiceId,
            @Valid @RequestBody FeeInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(feeService.updateInvoice(schoolId, invoiceId, request)));
    }

    @DeleteMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Delete invoice")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(
            @PathVariable Long schoolId,
            @PathVariable Long invoiceId) {
        feeService.deleteInvoice(schoolId, invoiceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice deleted."));
    }

    @GetMapping("/invoices/student/{studentId}")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get fee invoices for a specific student")
    public ResponseEntity<ApiResponse<Page<FeeInvoiceResponse>>> getInvoicesByStudent(
            @PathVariable Long schoolId,
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(feeService.getInvoicesByStudent(studentId, pageable)));
    }

    // ---- Razorpay ----

    @PostMapping("/payment/create-order")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'STUDENT', 'PARENT')")
    @Operation(summary = "Create Razorpay payment order",
               description = "Step 1: Create a Razorpay order. Returns orderId to be used by frontend Razorpay SDK.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @PathVariable Long schoolId,
            @Valid @RequestBody RazorpayOrderRequest request) {
        Map<String, Object> orderDetails = feeService.createRazorpayOrder(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(orderDetails, "Order created. Proceed with payment."));
    }

    @PostMapping("/payment/verify")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'STUDENT', 'PARENT')")
    @Operation(summary = "Verify Razorpay payment",
               description = "Step 2: Verify the payment signature from Razorpay callback and update invoice status.")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @PathVariable Long schoolId,
            @Valid @RequestBody PaymentVerifyRequest request) {
        PaymentResponse payment = feeService.verifyPayment(request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment verified successfully."));
    }

    // ---- Reports ----

    @GetMapping("/report/summary")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Fee collection summary report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFeeReport(
            @PathVariable Long schoolId,
            @RequestParam Integer year) {
        Map<String, Object> report = Map.of(
                "totalCollected", feeService.getTotalCollected(schoolId, year),
                "totalOutstanding", feeService.getTotalOutstanding(schoolId),
                "year", year,
                "currency", "INR"
        );
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
