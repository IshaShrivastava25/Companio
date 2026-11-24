package com.companio.service;

import com.companio.exception.ResourceNotFoundException;
import com.companio.model.TrustedContact;
import com.companio.model.User;
import com.companio.repository.TrustedContactRepository;
import com.companio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Stores and validates trusted-contact data for a user.
 */


@Service
public class TrustedContactService {

    private static final int MAX_CONTACTS = 5;

    @Autowired
    private TrustedContactRepository trustedContactRepository;

    @Autowired
    private UserRepository userRepository;

    public TrustedContact addContact(String userEmail, String name, String phoneNumber, String relationship) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (trustedContactRepository.findByUser_Id(user.getId()).size() >= MAX_CONTACTS) {
            throw new IllegalStateException("You can have up to " + MAX_CONTACTS + " trusted contacts");
        }

        TrustedContact contact = new TrustedContact(name, phoneNumber, relationship, user);
        return trustedContactRepository.save(contact);
    }

        public List<TrustedContact> getContactsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<TrustedContact> list = trustedContactRepository.findByUser_Id(user.getId());
        for (TrustedContact c : list) {
            if (c.getConnectCode() == null) {
                c.setConnectCode(TrustedContact.newCode());
                trustedContactRepository.save(c);
            }
        }
        return list;
    }

    public TrustedContact updateContact(String userEmail, Long contactId, String name, String phoneNumber, String relationship) {
        TrustedContact contact = findOwned(userEmail, contactId);
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setRelationship(relationship);
        return trustedContactRepository.save(contact);
    }

    public void deleteContact(String userEmail, Long contactId) {
        trustedContactRepository.delete(findOwned(userEmail, contactId));
    }

    private TrustedContact findOwned(String userEmail, Long id) {
        return trustedContactRepository.findById(id)
                .filter(c -> c.getUser().getEmail().equals(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
    }
}