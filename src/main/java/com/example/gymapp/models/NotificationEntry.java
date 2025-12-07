package com.example.gymapp.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class NotificationEntry {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private final StringProperty type = new SimpleStringProperty(this, "type", "");
    private final StringProperty message = new SimpleStringProperty(this, "message", "");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date", LocalDate.now());
    private final StringProperty status = new SimpleStringProperty(this, "status", "Pending");

    public NotificationEntry() {
    }

    public NotificationEntry(String type, String message, LocalDate date, String status) {
        this(0, type, message, date, status);
    }

    public NotificationEntry(int id, String type, String message, LocalDate date, String status) {
        this.id.set(id);
        this.type.set(type);
        this.message.set(message);
        this.date.set(date);
        this.status.set(status);
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

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
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
}
