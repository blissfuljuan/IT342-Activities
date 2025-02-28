package com.Lacaba.GoogleContacts.service;

import com.Lacaba.GoogleContacts.model.Contacts;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleContactsService {

    public List<Contacts> getContacts(OAuth2User principal) {
        // Dummy data, replace with actual Google Contacts API call
        List<Contacts> contacts = new ArrayList<>();
        contacts.add(new Contacts("Alice Johnson", "alice@example.com"));
        contacts.add(new Contacts("Bob Smith", "bob@example.com"));

        return contacts;
    }
}
