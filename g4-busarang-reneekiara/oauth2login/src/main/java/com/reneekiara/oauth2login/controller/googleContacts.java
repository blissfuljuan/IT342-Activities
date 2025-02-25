package com.reneekiara.oauth2login.controller;

import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.reneekiara.oauth2login.service.googleContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class googleContacts {

    @Autowired
    googleContactsService serv;

    @GetMapping("/contacts")
    public List<Person> getContacts(@AuthenticationPrincipal OAuth2User principal) throws IOException {
        return serv.getContacts(principal.getName());
    }
}

