package com.porcina.oauth2login.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contacts {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
}
