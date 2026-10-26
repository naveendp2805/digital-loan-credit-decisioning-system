package com.naveen.notification_service.repository;

import com.naveen.notification_service.entity.Notification;
import com.naveen.notification_service.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerId(Long customerId);

    List<Notification> findByLoanId(Long loanId);

    List<Notification> findByStatus(NotificationStatus status);

    Optional<Notification> findByEventKey(String eventKey);
}
