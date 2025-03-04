package com.quitco.googlecontactsapi.service;

import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ContactService {

    @Autowired
    private PeopleService peopleService;

    public List<Person> listContacts() throws IOException {
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();

        return response.getConnections();
    }

    public Person getContact(String resourceName) throws IOException {
        return peopleService.people().get(resourceName)
                .setPersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    public Person createContact(Person contact) throws IOException {
        return peopleService.people().createContact(contact).execute();
    }

    public Person updateContact(String resourceName, Person contact) throws IOException {
        return peopleService.people().updateContact(resourceName, contact)
                .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                .execute();
    }

    public void deleteContact(String resourceName) throws IOException {
        peopleService.people().deleteContact(resourceName).execute();
    }
}