package com.clabisellas.oauth2login.utilities;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

import java.util.List;
import java.util.stream.Collectors;

public class ContactUtil {

    public static String getDisplayName(Person person) {
        if (person == null || person.getNames() == null || person.getNames().isEmpty()) {
            return "No Name";
        }
        
        Name name = person.getNames().get(0);
        String displayName = name.getDisplayName();
        
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        
        String firstName = name.getGivenName() != null ? name.getGivenName() : "";
        String lastName = name.getFamilyName() != null ? name.getFamilyName() : "";
        
        return (firstName + " " + lastName).trim();
    }
    
    public static String getFirstName(Person person) {
        if (person == null || person.getNames() == null || person.getNames().isEmpty()) {
            return "";
        }
        
        Name name = person.getNames().get(0);
        return name.getGivenName() != null ? name.getGivenName() : "";
    }
    
    public static String getLastName(Person person) {
        if (person == null || person.getNames() == null || person.getNames().isEmpty()) {
            return "";
        }
        
        Name name = person.getNames().get(0);
        return name.getFamilyName() != null ? name.getFamilyName() : "";
    }
    
    public static String getPrimaryEmail(Person person) {
        if (person == null || person.getEmailAddresses() == null || person.getEmailAddresses().isEmpty()) {
            return "";
        }
        
        return person.getEmailAddresses().get(0).getValue();
    }
    
    public static List<String> getEmails(Person person) {
        if (person == null || person.getEmailAddresses() == null || person.getEmailAddresses().isEmpty()) {
            return List.of();
        }

        return person.getEmailAddresses().stream()
                .map(EmailAddress::getValue)
                .collect(Collectors.toList());
    }
    
    public static String getPrimaryPhone(Person person) {
        if (person == null || person.getPhoneNumbers() == null || person.getPhoneNumbers().isEmpty()) {
            return "";
        }
        
        return person.getPhoneNumbers().get(0).getValue();
    }
    
    public static List<String> getPhoneNumbers(Person person) {
        if (person == null || person.getPhoneNumbers() == null || person.getPhoneNumbers().isEmpty()) {
            return List.of();
        }

        return person.getPhoneNumbers().stream()
                .map(PhoneNumber::getValue)
                .collect(Collectors.toList());
    }
    
    public static String getPhotoUrl(Person person) {
        if (person == null || person.getPhotos() == null || person.getPhotos().isEmpty()) {
            return "/images/default-avatar.png";
        }
        
        return person.getPhotos().get(0).getUrl();
    }
}
