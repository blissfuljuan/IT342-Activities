package com.Enriquez.GoogleAPIIntegration.DTO;

public class Contact {
    private String name;
    private String email;
    private String phone;
    private String resourceName;

    // Constructors (default and parameterized)
    public Contact(){

    }

    public Contact(String phone, String email, String name,String resourceName) {
            this.name =  name;
            this.email = email;
            this.phone = phone;
            this.resourceName = resourceName;

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
        return resourceName;
    }
    public void setId(String resourceName){
        this. resourceName = resourceName;
    }
}
