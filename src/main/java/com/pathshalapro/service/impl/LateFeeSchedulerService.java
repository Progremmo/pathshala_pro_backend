package com.pathshalapro.service.impl;

import com.pathshalapro.entity.FeeInvoice;
import com.pathshalapro.entity.LateFeeRule;
import com.pathshalapro.entity.enums.PaymentStatus;
import com.pathshalapro.repository.FeeInvoiceRepository;
import com.pathshalapro.repository.LateFeeRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LateFeeSchedulerService {

    private final FeeInvoiceRepository feeInvoiceRepository;
    private final LateFeeRuleRepository lateFeeRuleRepository;

    /**
     * Runs every day at 1:00 AM to calculate and apply late fees to overdue invoices.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void calculateLateFees() {
        log.info("Starting late fee calculation...");
        LocalDate today = LocalDate.now();

        // Get all unpaid invoices that are past their due date
        List<FeeInvoice> overdueInvoices = feeInvoiceRepository.findByPaymentStatusNotAndIsDeletedFalse(PaymentStatus.PAID)
                .stream()
                .filter(inv -> inv.getDueDate().isBefore(today))
                .toList();

        for (FeeInvoice invoice : overdueInvoices) {
            Long schoolId = invoice.getSchool().getId();
            List<LateFeeRule> rules = lateFeeRuleRepository.findBySchoolId(schoolId);

            if (rules.isEmpty()) continue;

            long daysOverdue = ChronoUnit.DAYS.between(invoice.getDueDate(), today);

            BigDecimal fineAmount = BigDecimal.ZERO;
            for (LateFeeRule rule : rules) {
                if (daysOverdue > rule.getGracePeriodDays()) {
                    if ("FIXED".equalsIgnoreCase(rule.getRuleType())) {
                        fineAmount = fineAmount.add(rule.getRuleValue());
                    } else if ("PERCENTAGE".equalsIgnoreCase(rule.getRuleType())) {
                        BigDecimal percent = rule.getRuleValue().divide(BigDecimal.valueOf(100));
                        fineAmount = fineAmount.add(invoice.getTotalAmount().multiply(percent));
                    }
                }
            }

            if (fineAmount.compareTo(invoice.getFineAmount()) > 0) {
                invoice.setFineAmount(fineAmount);
                invoice.setNetAmount(invoice.getTotalAmount().subtract(invoice.getDiscountAmount()).add(fineAmount));
                feeInvoiceRepository.save(invoice);
                log.info("Applied late fee of {} to invoice {}", fineAmount, invoice.getInvoiceNumber());
            }
        }
        log.info("Late fee calculation completed.");
    }
}
