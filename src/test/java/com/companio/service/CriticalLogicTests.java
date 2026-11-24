package com.companio.service;

import com.companio.exception.ResourceNotFoundException;
import com.companio.model.SosEvent;
import com.companio.model.SosStatus;
import com.companio.model.TrustedContact;
import com.companio.model.User;
import com.companio.repository.LocationUpdateRepository;
import com.companio.repository.SosEventRepository;
import com.companio.repository.TrustedContactRepository;
import com.companio.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriticalLogicTests {

    @Mock private NotificationService notificationService;
    @Mock private SosEventRepository sosEventRepository;
    @Mock private UserRepository userRepository;
    @Mock private LocationUpdateRepository locationUpdateRepository;
    @Mock private TrustedContactRepository trustedContactRepository;
    @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @InjectMocks private SosEventService sosService;
    @InjectMocks private TrustedContactService contactService;

    private User owner() {
        User u = new User("Owner", "owner@test.com", "password1", "9876543210");
        u.setPinHash(passwordEncoder.encode("1234"));
        u.setDuressPinHash(passwordEncoder.encode("9876"));
        return u;
    }

    private SosEvent pendingEvent() {
        SosEvent e = new SosEvent(owner(), 30.0, 78.0);
        when(sosEventRepository.findById(1L)).thenReturn(Optional.of(e));
        lenient().when(sosEventRepository.save(any(SosEvent.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        return e;
    }

    @Test
    void realPinCancelsTheSos() {
        pendingEvent();
        SosEvent result = sosService.cancelSos("owner@test.com", 1L, "1234");
        assertEquals(SosStatus.CANCELLED, result.getStatus());
    }

    @Test
    void duressPinLooksLikeSuccessButKeepsTheAlert() {
        pendingEvent();
        SosEvent result = sosService.cancelSos("owner@test.com", 1L, "9876");
        assertEquals(SosStatus.PENDING, result.getStatus());
        verify(sosEventRepository, never()).save(any(SosEvent.class));
    }

    @Test
    void wrongPinIsRejected() {
        pendingEvent();
        assertThrows(IllegalArgumentException.class,
                () -> sosService.cancelSos("owner@test.com", 1L, "0000"));
    }

    @Test
    void anotherUserCannotCancelSomeoneElsesSos() {
        pendingEvent();
        assertThrows(ResourceNotFoundException.class,
                () -> sosService.cancelSos("other@test.com", 1L, "1234"));
    }

    @Test
    void anotherUserCannotEditSomeoneElsesContact() {
        TrustedContact c = new TrustedContact("Mom", "9999999999", "Mother", owner());
        when(trustedContactRepository.findById(5L)).thenReturn(Optional.of(c));
        assertThrows(ResourceNotFoundException.class,
                () -> contactService.updateContact("other@test.com", 5L, "X", "9999999999", "Y"));
    }

    @Test
    void trackingLinkDoesNotWorkWhileSosIsStillPending() {
        SosEvent e = new SosEvent(owner(), 30.0, 78.0);
        when(sosEventRepository.findByTrackingToken("tok")).thenReturn(Optional.of(e));
        assertThrows(ResourceNotFoundException.class, () -> sosService.getTracking("tok"));
    }
}

