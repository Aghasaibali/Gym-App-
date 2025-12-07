package com.example.gymapp.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Member {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private final StringProperty name = new SimpleStringProperty(this, "name", "");
    private final StringProperty email = new SimpleStringProperty(this, "email", "");
    private final StringProperty membershipType = new SimpleStringProperty(this, "membershipType", "");

    public Member() {
    }

    public Member(String name, String email, String membershipType) {
        this(0, name, email, membershipType);
    }

    public Member(int id, String name, String email, String membershipType) {
        this.id.set(id);
        this.name.set(name);
        this.email.set(email);
        this.membershipType.set(membershipType);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getMembershipType() {
        return membershipType.get();
    }

    public StringProperty membershipTypeProperty() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType.set(membershipType);
    }
}
