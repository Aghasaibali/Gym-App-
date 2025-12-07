package com.example.gymapp.models;

import javafx.beans.property.*;

import java.time.LocalDate;

public class ClassSession {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private final StringProperty className = new SimpleStringProperty(this, "className", "");
    private final StringProperty instructor = new SimpleStringProperty(this, "instructor", "");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date", LocalDate.now());
    private final StringProperty time = new SimpleStringProperty(this, "time", "");
    private final IntegerProperty capacity = new SimpleIntegerProperty(this, "capacity", 0);
    private final IntegerProperty enrolled = new SimpleIntegerProperty(this, "enrolled", 0);

    public ClassSession() {
    }

    public ClassSession(String className, String instructor, LocalDate date, String time, int capacity, int enrolled) {
        this(0, className, instructor, date, time, capacity, enrolled);
    }

    public ClassSession(int id, String className, String instructor, LocalDate date, String time, int capacity, int enrolled) {
        this.id.set(id);
        this.className.set(className);
        this.instructor.set(instructor);
        this.date.set(date);
        this.time.set(time);
        this.capacity.set(capacity);
        this.enrolled.set(enrolled);
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

    public String getClassName() {
        return className.get();
    }

    public StringProperty classNameProperty() {
        return className;
    }

    public void setClassName(String className) {
        this.className.set(className);
    }

    public String getInstructor() {
        return instructor.get();
    }

    public StringProperty instructorProperty() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor.set(instructor);
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

    public String getTime() {
        return time.get();
    }

    public StringProperty timeProperty() {
        return time;
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public int getCapacity() {
        return capacity.get();
    }

    public IntegerProperty capacityProperty() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity.set(capacity);
    }

    public int getEnrolled() {
        return enrolled.get();
    }

    public IntegerProperty enrolledProperty() {
        return enrolled;
    }

    public void setEnrolled(int enrolled) {
        this.enrolled.set(enrolled);
    }
}
