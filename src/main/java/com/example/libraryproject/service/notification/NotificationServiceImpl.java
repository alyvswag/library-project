package com.example.libraryproject.service.notification;

import com.example.libraryproject.exception.BaseException;
import com.example.libraryproject.mapper.notification.NotificationMapper;
import com.example.libraryproject.model.dao.*;
import com.example.libraryproject.model.dto.request.create.NotificationRequestCreate;
import com.example.libraryproject.model.dto.response.admin.NotificationResponseAdmin;
import com.example.libraryproject.model.enums.notification.DataType;
import com.example.libraryproject.model.enums.notification.NotificationStatus;
import com.example.libraryproject.repository.book.BookRepository;
import com.example.libraryproject.repository.event.EventRepository;
import com.example.libraryproject.repository.notification.NotificationRepository;
import com.example.libraryproject.repository.reminder.ReminderRepository;
import com.example.libraryproject.repository.user.UserRepository;
import com.example.libraryproject.service.email.EmailProducer;
import com.example.libraryproject.service.email.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.libraryproject.model.enums.notification.DataType.*;
import static com.example.libraryproject.model.enums.notification.NotificationStatus.DELETED;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    final NotificationMapper notificationMapper;
    final NotificationRepository notificationRepository;
    final ReminderRepository reminderRepository;
    final BookRepository bookRepository;
    final UserRepository userRepository;
    final EventRepository eventRepository;
    final EmailProducer emailProducer;
    private final EmailService emailService;

    @Override
    public void sendReminderNotification(Long reminderId) {
        Reminder reminderEntity = reminderRepository.findReminderById(reminderId)
                .orElseThrow(() -> BaseException.notFound(Reminder.class.getSimpleName(), "reminder", String.valueOf(reminderId)));

        Long daysBetween = ChronoUnit.DAYS.between( LocalDate.now(),(reminderEntity.getReminderDate().plusDays(1)));
        emailProducer.sendReminderNotification(reminderEntity.getUser().getEmail(), reminderEntity.getBook().getBookName(),String.valueOf(daysBetween));

        notificationRepository.save(notificationMapper.toEntity(NotificationRequestCreate.builder()
                .userId(reminderEntity.getUser().getId())
                .dataType(REMINDER)
                .message("Sizin kirayə götürdüyünüz kitabın (" + reminderEntity.getBook().getBookName() + ") kirayə müddətinin bitməsinə " + daysBetween+" gün qalıb.")
                .build()
        ));
    }

    @Override
    public void sendNewBookNotification(Long bookId, Long userId) {
        Book bookEntity = bookRepository.findBookById(bookId)
                .orElseThrow(
                        () -> BaseException.notFound(Book.class.getSimpleName(), "book", String.valueOf(bookId))
                );
        User userEntity = userRepository.findUserById(userId)
                .orElseThrow(
                        () -> BaseException.notFound(User.class.getSimpleName(), "user", String.valueOf(userId))
                );
        emailProducer.sendBookNotification(userEntity.getEmail(), String.valueOf(bookId), bookEntity.getBookName());
        notificationRepository.save(notificationMapper.toEntity(NotificationRequestCreate.builder()
                .userId(userId)
                .dataType(BOOK)
                .message("Kitabxanamızda yeni " + bookEntity.getBookName() + " kitabı var")
                .build()
        ));
    }

    @Override
    public void sendEventNotification(Long eventId, Long userId) {
        Event eventEntity = eventRepository.findEventById(eventId)
                .orElseThrow(() -> BaseException.notFound(Event.class.getSimpleName(), "event", String.valueOf(eventId)));
        User userEntity = userRepository.findUserById(userId)
                .orElseThrow(
                        () -> BaseException.notFound(User.class.getSimpleName(), "user", String.valueOf(userId))
                );

        emailProducer.sendEventNotification(userEntity.getEmail(), eventEntity.getEventName());

        notificationRepository.save(notificationMapper.toEntity(NotificationRequestCreate.builder()
                .userId(userId)
                .dataType(EVENT)
                .message("Kitabxanamızda " + eventEntity.getEventName() + " tedbiri olacaq. Dəvətlisiniz")
                .build()
        ));
    }
    @Override
    public List<NotificationResponseAdmin> getNotificationsByUser(Long userId){
        return notificationMapper.toDto(notificationRepository.findNotificationsByUserId(userId));
    }

    @Override
    public void removeNotification(Long notificationId) {
        Notification notificationEntity = notificationRepository.findNotificationById(notificationId).orElseThrow(
                () -> BaseException.notFound(Notification.class.getSimpleName(), "notification", String.valueOf(notificationId))
        );
        notificationEntity.setNotificationStatus(DELETED);
        notificationRepository.save(notificationEntity);
    }


}
