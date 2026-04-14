package com.pathshalapro.repository;

import com.pathshalapro.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<Notification> findByRecipientIdAndIsReadFalseAndIsDeletedFalse(Long userId, Pageable pageable);

    Long countByRecipientIdAndIsReadFalseAndIsDeletedFalse(Long userId);

    Page<Notification> findBySchoolIdAndRecipientIsNullAndIsDeletedFalse(Long schoolId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isDeleted = false")
    void markAllAsReadByUser(@Param("userId") Long userId);
}
