package com.example.contacts.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    private String resourceName;
    private String etag;
    private String displayName;
    private String givenName;
    private String familyName;
    
    @Builder.Default
    private List<EmailAddress> emailAddresses = new ArrayList<>();
    
    @Builder.Default
    private List<PhoneNumber> phoneNumbers = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailAddress {
        private String value;
        private String type; 
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneNumber {
        private String value;
        private String type; 
    }
}
