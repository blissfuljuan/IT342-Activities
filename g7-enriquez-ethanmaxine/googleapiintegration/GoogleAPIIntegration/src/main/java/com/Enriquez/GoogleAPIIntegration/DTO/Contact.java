package com.Enriquez.GoogleAPIIntegration.DTO;

public class Contact {
    private String name;
    private String email;
    private String phone;
    private String id;

    // Constructors (default and parameterized)
    public Contact(){

    }

    public Contact(String phone, String email, String name, String id) {
            this.name =  name;
            this.email = email;
            this.phone = phone;
            this.id = id;

    }
    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
}
