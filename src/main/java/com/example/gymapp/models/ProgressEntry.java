package com.example.gymapp.models;

import javafx.beans.property.*;

import java.time.LocalDate;

public class ProgressEntry {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private final StringProperty memberName = new SimpleStringProperty(this, "memberName", "");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date", LocalDate.now());
    private final DoubleProperty weight = new SimpleDoubleProperty(this, "weight", 0.0);
    private final DoubleProperty bodyFat = new SimpleDoubleProperty(this, "bodyFat", 0.0);
    private final StringProperty note = new SimpleStringProperty(this, "note", "");

    public ProgressEntry() {
    }

    public ProgressEntry(String memberName, LocalDate date, double weight, double bodyFat, String note) {
        this(0, memberName, date, weight, bodyFat, note);
    }

    public ProgressEntry(int id, String memberName, LocalDate date, double weight, double bodyFat, String note) {
        this.id.set(id);
        this.memberName.set(memberName);
        this.date.set(date);
        this.weight.set(weight);
        this.bodyFat.set(bodyFat);
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

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public double getWeight() {
        return weight.get();
    }

    public DoubleProperty weightProperty() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight.set(weight);
    }

    public double getBodyFat() {
        return bodyFat.get();
    }

    public DoubleProperty bodyFatProperty() {
        return bodyFat;
    }

    public void setBodyFat(double bodyFat) {
        this.bodyFat.set(bodyFat);
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
