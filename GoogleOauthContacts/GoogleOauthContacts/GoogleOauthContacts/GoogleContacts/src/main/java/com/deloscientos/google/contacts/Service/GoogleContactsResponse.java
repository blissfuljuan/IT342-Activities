package com.deloscientos.google.contacts.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleContactsResponse {
    private List<Person> connections;

    public List<Person> getConnections() {
        return connections;
    }

    public void setConnections(List<Person> connections) {
        this.connections = connections;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Person {
        private String resourceName;
        private String etag;  // ✅ Add etag field
        private List<Name> names;
        private List<EmailAddress> emailAddresses;
        private List<PhoneNumber> phoneNumbers;
        private List<Birthday> birthdays;
        

        public String getResourceName() {
            return resourceName;
        }

        public String getEtag() {  // ✅ Getter for etag
            return etag;
        }

        public List<Name> getNames() {
            return names;
        }

        public List<EmailAddress> getEmailAddresses() {
            return emailAddresses;
        }
        public List<PhoneNumber> getPhoneNumbers() {
            return phoneNumbers;
        }

        public List<Birthday> getBirthdays() {
            return birthdays;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Name {
            private String displayName;

            public String getDisplayName() {
                return displayName;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EmailAddress {
            private String value;

            public String getValue() {
                return value;
            }
        }
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Birthday {
            private Date date;

            public Date getDate() {
                return date;
            }
        }
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PhoneNumber {
            private String value;

            public String getValue() {
                return value;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Date {
            private Integer year;
            private Integer month;
            private Integer day;

            public Integer getYear() {
                return year;
            }

            public Integer getMonth() {
                return month;
            }

            public Integer getDay() {
                return day;
            }
        }
    }
}
