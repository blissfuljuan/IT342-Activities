package com.deloscientos.google.contacts.dto;

public class Metadata {
    private String etag; // ✅ This will hold the etag value

    public Metadata() {}

    public Metadata(String etag) {
        this.etag = etag;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
