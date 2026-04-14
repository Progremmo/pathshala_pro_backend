package com.pathshalapro.repository;

import com.pathshalapro.entity.Payment;
import com.pathshalapro.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderIdAndIsDeletedFalse(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentIdAndIsDeletedFalse(String razorpayPaymentId);

    Page<Payment> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    Page<Payment> findByFeeInvoiceIdAndIsDeletedFalse(Long invoiceId, Pageable pageable);

    Page<Payment> findBySchoolIdAndStatusAndIsDeletedFalse(Long schoolId, PaymentStatus status, Pageable pageable);
}
