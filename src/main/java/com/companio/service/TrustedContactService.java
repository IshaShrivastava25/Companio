package com.companio.service;

import com.companio.model.TrustedContact;
import com.companio.model.User;
import com.companio.repository.TrustedContactRepository;
import com.companio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrustedContactService {

    @Autowired
    private TrustedContactRepository trustedContactRepository;

    @Autowired
    private UserRepository userRepository;

    public TrustedContact addContact(String userEmail, String name, String phoneNumber, String relationship) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TrustedContact contact = new TrustedContact(name, phoneNumber, relationship, user);
        return trustedContactRepository.save(contact);
    }

    public List<TrustedContact> getContactsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return trustedContactRepository.findByUser_Id(user.getId());
    }
}

