package com.pathshalapro.repository;

import com.pathshalapro.entity.FeeInvoice;
import com.pathshalapro.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {

    Optional<FeeInvoice> findByInvoiceNumberAndIsDeletedFalse(String invoiceNumber);

    Page<FeeInvoice> findBySchoolIdAndAcademicYearAndIsDeletedFalse(Long schoolId, String academicYear, Pageable pageable);

    Page<FeeInvoice> findByStudentIdAndIsDeletedFalse(Long studentId, Pageable pageable);

    Page<FeeInvoice> findByStudentIdAndAcademicYearAndIsDeletedFalse(Long studentId, String academicYear, Pageable pageable);
    
    List<FeeInvoice> findBySchoolIdAndAcademicYearAndIsDeletedFalse(Long schoolId, String academicYear);

    Page<FeeInvoice> findBySchoolIdAndPaymentStatusAndIsDeletedFalse(Long schoolId, PaymentStatus status, Pageable pageable);

    List<FeeInvoice> findByStudentIdAndPaymentStatusAndIsDeletedFalse(Long studentId, PaymentStatus status);

    List<FeeInvoice> findByPaymentStatusNotAndIsDeletedFalse(PaymentStatus status);

    List<FeeInvoice> findBySchoolIdAndDueDateBeforeAndPaymentStatusNotAndIsDeletedFalse(
            Long schoolId, LocalDate date, PaymentStatus status);

    @Query("SELECT SUM(fi.netAmount) FROM FeeInvoice fi WHERE fi.school.id = :schoolId AND " +
           "fi.paymentStatus = 'PAID' AND fi.isDeleted = false AND fi.periodYear = :year")
    BigDecimal getTotalCollectedBySchoolAndYear(@Param("schoolId") Long schoolId, @Param("year") Integer year);

    @Query("SELECT SUM(fi.netAmount - fi.paidAmount) FROM FeeInvoice fi WHERE fi.school.id = :schoolId AND " +
           "fi.paymentStatus IN ('PENDING', 'PARTIAL') AND fi.isDeleted = false")
    BigDecimal getTotalOutstandingBySchool(@Param("schoolId") Long schoolId);

    boolean existsByStudentIdAndFeeStructureIdAndPeriodMonthAndPeriodYearAndIsDeletedFalse(
            Long studentId, Long feeStructureId, Integer month, Integer year);

    boolean existsByStudentIdAndPeriodMonthAndPeriodYearAndAcademicYearAndIsDeletedFalse(
            Long studentId, Integer month, Integer year, String academicYear);
    @Query("SELECT SUM(fi.netAmount) FROM FeeInvoice fi WHERE fi.school.id = :schoolId AND fi.paymentStatus = 'PAID' AND fi.isDeleted = false AND fi.updatedAt >= :startDate")
    BigDecimal getCollectionSince(@Param("schoolId") Long schoolId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(fi.netAmount - fi.paidAmount) FROM FeeInvoice fi WHERE fi.school.id = :schoolId AND fi.paymentStatus IN ('PENDING', 'PARTIAL') AND fi.isDeleted = false AND fi.dueDate >= :startDate")
    BigDecimal getOutstandingSince(@Param("schoolId") Long schoolId, @Param("startDate") LocalDate startDate);
}
