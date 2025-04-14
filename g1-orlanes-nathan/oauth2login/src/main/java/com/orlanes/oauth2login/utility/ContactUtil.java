package com.orlanes.oauth2login.utility;

import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

//import java.util.List;

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
        
        EmailAddress email = person.getEmailAddresses().get(0);
        return email.getValue() != null ? email.getValue() : "";
    }
    
    public static String getPrimaryPhone(Person person) {
        if (person == null || person.getPhoneNumbers() == null || person.getPhoneNumbers().isEmpty()) {
            return "";
        }
        
        PhoneNumber phone = person.getPhoneNumbers().get(0);
        return phone.getValue() != null ? phone.getValue() : "";
    }
    
    public static String getPhotoUrl(Person person) {
        if (person == null || person.getPhotos() == null || person.getPhotos().isEmpty()) {
            return "/images/default-avatar.png";
        }
        
        return person.getPhotos().get(0).getUrl();
    }
}

