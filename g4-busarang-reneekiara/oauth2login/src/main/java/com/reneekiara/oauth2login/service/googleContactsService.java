package com.reneekiara.oauth2login.service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.util.List;

@Service
public class googleContactsService {

    PeopleService peopleService;
    public List getContacts(String principalName) throws IOException {

        ListConnectionsResponse response = peopleService.people().connections().list("people/me")
                .setPersonFields("names,emailAddresses")
                .execute();
        List<Person> people = response.getConnections();
            return people;

    }

}
