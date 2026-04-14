package com.pathshalapro.repository;

import com.pathshalapro.entity.SchoolSubscription;
import com.pathshalapro.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolSubscriptionRepository extends JpaRepository<SchoolSubscription, Long> {

    Optional<SchoolSubscription> findBySchoolIdAndIsDeletedFalse(Long schoolId);

    @Query("SELECT ss FROM SchoolSubscription ss WHERE ss.status = :status AND ss.endDate < :date AND ss.isDeleted = false")
    List<SchoolSubscription> findExpiredSubscriptions(@Param("status") SubscriptionStatus status,
                                                       @Param("date") LocalDate date);
}
