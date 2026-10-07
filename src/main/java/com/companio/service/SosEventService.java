package com.companio.service;

import com.companio.model.SosEvent;
import com.companio.model.SosStatus;
import com.companio.model.User;
import com.companio.repository.SosEventRepository;
import com.companio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SosEventService {

    @Autowired
    private SosEventRepository sosEventRepository;

    @Autowired
    private UserRepository userRepository;

    public SosEvent triggerSos(String userEmail, Double latitude, Double longitude) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SosEvent event = new SosEvent(user, latitude, longitude);
        return sosEventRepository.save(event);
    }

    public List<SosEvent> getMyEvents(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sosEventRepository.findByUser_Id(user.getId());
    }

    public SosEvent resolveSos(String userEmail, Long eventId) {
        SosEvent event = sosEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("SOS event not found"));

        if (!event.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Not authorized to resolve this event");
        }

        event.setStatus(SosStatus.RESOLVED);
        event.setResolvedAt(LocalDateTime.now());
        return sosEventRepository.save(event);
    }
}
