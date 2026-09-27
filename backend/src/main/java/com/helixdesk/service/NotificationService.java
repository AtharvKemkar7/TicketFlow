package com.helixdesk.service;

import com.helixdesk.entity.Notification;
import com.helixdesk.entity.Ticket;
import com.helixdesk.entity.UserAccount;
import com.helixdesk.enums.NotificationType;
import com.helixdesk.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void notify(UserAccount recipient, Ticket ticket, NotificationType type, String message) {
        if (recipient == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTicket(ticket);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    public List<Notification> forUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFlagFalse(userId);
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().getId().equals(userId)) {
                n.setReadFlag(true);
            }
        });
    }
}
