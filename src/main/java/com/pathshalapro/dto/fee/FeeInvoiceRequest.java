package com.pathshalapro.dto.fee;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FeeInvoiceRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Fee structure ID is required")
    private Long feeStructureId;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal fineAmount;

    private Integer periodMonth;
    private Integer periodYear;
    private String academicYear;
    private String remarks;
}
