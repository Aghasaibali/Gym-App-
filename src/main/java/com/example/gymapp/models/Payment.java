package com.example.gymapp.models;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Payment {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private final StringProperty memberName = new SimpleStringProperty(this, "memberName", "");
    private final DoubleProperty amount = new SimpleDoubleProperty(this, "amount", 0.0);
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date", LocalDate.now());
    private final StringProperty status = new SimpleStringProperty(this, "status", "Pending");
    private final StringProperty note = new SimpleStringProperty(this, "note", "");

    public Payment() {
    }

    public Payment(String memberName, double amount, LocalDate date, String status, String note) {
        this(0, memberName, amount, date, status, note);
    }

    public Payment(int id, String memberName, double amount, LocalDate date, String status, String note) {
        this.id.set(id);
        this.memberName.set(memberName);
        this.amount.set(amount);
        this.date.set(date);
        this.status.set(status);
        this.note.set(note);
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

    public String getMemberName() {
        return memberName.get();
    }

    public StringProperty memberNameProperty() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName.set(memberName);
    }

    public double getAmount() {
        return amount.get();
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getNote() {
        return note.get();
    }

    public StringProperty noteProperty() {
        return note;
    }

    public void setNote(String note) {
        this.note.set(note);
    }
}
