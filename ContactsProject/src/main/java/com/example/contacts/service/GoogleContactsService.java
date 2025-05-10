package com.example.contacts.service;

import com.example.contacts.model.Contact;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
@RequiredArgsConstructor
public class GoogleContactsService {

    private final OAuth2AuthorizedClientService clientService;

    public List<Contact> getContacts(OAuth2AuthenticationToken auth, String sort) {
        try {
            PeopleService service = getPeopleService(auth);
            List<Person> connections = service.people().connections()
                    .list("people/me")
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute().getConnections();
            
            List<Contact> contacts = connections == null ? Collections.emptyList() :
                    connections.stream().map(this::mapToContact).collect(Collectors.toList());

            if (sort != null) {
                switch (sort) {
                    case "name":
                        contacts.sort(Comparator.comparing(Contact::getDisplayName, Comparator.nullsLast(Comparator.naturalOrder())));
                        break;
                    case "email":
                        contacts.sort(Comparator.comparing(contact -> 
                            contact.getEmailAddresses() == null || contact.getEmailAddresses().isEmpty() ? "" : 
                            contact.getEmailAddresses().get(0).getValue(),
                            Comparator.nullsLast(Comparator.naturalOrder())));
                        break;
                    case "nameDesc":
                        contacts.sort(Comparator.comparing(Contact::getDisplayName, Comparator.nullsLast(Comparator.reverseOrder())));
                        break;
                    case "emailDesc":
                        contacts.sort(Comparator.comparing(contact -> 
                            contact.getEmailAddresses() == null || contact.getEmailAddresses().isEmpty() ? "" : 
                            contact.getEmailAddresses().get(0).getValue(),
                            Comparator.nullsLast(Comparator.reverseOrder())));
                        break;
                }
            }
            return contacts;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Contact getContact(OAuth2AuthenticationToken auth, String resourceName) {
        try {
            PeopleService service = getPeopleService(auth);
            return mapToContact(service.people().get(resourceName)
                    .setPersonFields("names,emailAddresses,phoneNumbers")
                    .execute());
        } catch (Exception e) {
            return null;
        }
    }

    public Contact createContact(OAuth2AuthenticationToken auth, Contact contact) {
        try {
            PeopleService service = getPeopleService(auth);
            Person person = new Person()
                    .setNames(Collections.singletonList(new Name()
                            .setGivenName(contact.getGivenName())
                            .setFamilyName(contact.getFamilyName())
                            .setDisplayName(contact.getDisplayName())))
                    .setEmailAddresses(mapEmails(contact))
                    .setPhoneNumbers(mapPhones(contact));
            return mapToContact(service.people().createContact(person).execute());
        } catch (Exception e) {
            return null;
        }
    }

    public Contact updateContact(OAuth2AuthenticationToken auth, Contact contact) {
        try {
            PeopleService service = getPeopleService(auth);
            Person person = new Person()
                    .setEtag(contact.getEtag())
                    .setNames(Collections.singletonList(new Name()
                            .setGivenName(contact.getGivenName())
                            .setFamilyName(contact.getFamilyName())
                            .setDisplayName(contact.getDisplayName())))
                    .setEmailAddresses(mapEmails(contact))
                    .setPhoneNumbers(mapPhones(contact));
            return mapToContact(service.people()
                    .updateContact(contact.getResourceName(), person)
                    .setUpdatePersonFields("names,emailAddresses,phoneNumbers")
                    .execute());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteContact(OAuth2AuthenticationToken auth, String resourceName) {
        try {
            getPeopleService(auth).people().deleteContact(resourceName).execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private PeopleService getPeopleService(OAuth2AuthenticationToken auth) throws Exception {
        return new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new GoogleCredential().setAccessToken(
                        clientService.loadAuthorizedClient(
                                auth.getAuthorizedClientRegistrationId(),
                                auth.getName()).getAccessToken().getTokenValue())
        ).setApplicationName("Google Contacts Integration").build();
    }

    private Contact mapToContact(Person person) {
        Contact contact = new Contact();
        contact.setResourceName(person.getResourceName());
        contact.setEtag(person.getEtag());

        if (person.getNames() != null && !person.getNames().isEmpty()) {
            Name name = person.getNames().get(0);
            contact.setDisplayName(name.getDisplayName());
            contact.setGivenName(name.getGivenName());
            contact.setFamilyName(name.getFamilyName());
        }

        if (person.getEmailAddresses() != null) {
            contact.setEmailAddresses(person.getEmailAddresses().stream()
                    .map(email -> new Contact.EmailAddress(email.getValue(), email.getType()))
                    .collect(Collectors.toList()));
        }

        if (person.getPhoneNumbers() != null) {
            contact.setPhoneNumbers(person.getPhoneNumbers().stream()
                    .map(phone -> new Contact.PhoneNumber(phone.getValue(), phone.getType()))
                    .collect(Collectors.toList()));
        }

        return contact;
    }

    private List<EmailAddress> mapEmails(Contact contact) {
        return contact.getEmailAddresses() == null ? Collections.emptyList() :
                contact.getEmailAddresses().stream()
                        .map(email -> new EmailAddress().setValue(email.getValue()).setType(email.getType()))
                        .collect(Collectors.toList());
    }

    private List<PhoneNumber> mapPhones(Contact contact) {
        return contact.getPhoneNumbers() == null ? Collections.emptyList() :
                contact.getPhoneNumbers().stream()
                        .map(phone -> new PhoneNumber().setValue(phone.getValue()).setType(phone.getType()))
                        .collect(Collectors.toList());
    }
}
