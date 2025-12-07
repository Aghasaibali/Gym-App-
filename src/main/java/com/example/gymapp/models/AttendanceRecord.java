package com.example.gymapp.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;

public class AttendanceRecord {

    private final IntegerProperty id = new SimpleIntegerProperty(this, "id", 0);
    private final StringProperty memberName = new SimpleStringProperty(this, "memberName", "");
    private final StringProperty type = new SimpleStringProperty(this, "type", "");
    private final ObjectProperty<LocalDateTime> time = new SimpleObjectProperty<>(this, "time", LocalDateTime.now());

    public AttendanceRecord() {
    }

    public AttendanceRecord(String memberName, String type, LocalDateTime time) {
        this(0, memberName, type, time);
    }

    public AttendanceRecord(int id, String memberName, String type, LocalDateTime time) {
        this.id.set(id);
        this.memberName.set(memberName);
        this.type.set(type);
        this.time.set(time);
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

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public LocalDateTime getTime() {
        return time.get();
    }

    public ObjectProperty<LocalDateTime> timeProperty() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time.set(time);
    }
}
