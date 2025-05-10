package com.malinao.oauth2login.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
}

