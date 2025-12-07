package com.example.gymapp.controllers;

import com.example.gymapp.db.GymRepository;
import com.example.gymapp.models.AttendanceRecord;
import com.example.gymapp.models.ClassSession;
import com.example.gymapp.models.Member;
import com.example.gymapp.models.NotificationEntry;
import com.example.gymapp.models.Payment;
import com.example.gymapp.models.ProgressEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    // Members
    @FXML
    private TableView<Member> memberTable;
    @FXML
    private TableColumn<Member, String> nameColumn;
    @FXML
    private TableColumn<Member, String> emailColumn;
    @FXML
    private TableColumn<Member, String> membershipColumn;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField membershipField;

    private final ObservableList<Member> memberData = FXCollections.observableArrayList();
    private final ObservableList<String> memberNames = FXCollections.observableArrayList();

    // Payments
    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, String> payMemberColumn;
    @FXML
    private TableColumn<Payment, String> payAmountColumn;
    @FXML
    private TableColumn<Payment, String> payStatusColumn;
    @FXML
    private TableColumn<Payment, String> payDateColumn;
    @FXML
    private TableColumn<Payment, String> payNoteColumn;
    @FXML
    private ComboBox<String> payMemberCombo;
    @FXML
    private TextField payAmountField;
    @FXML
    private ChoiceBox<String> payStatusChoice;
    @FXML
    private DatePicker payDatePicker;
    @FXML
    private TextField payNoteField;

    private final ObservableList<Payment> paymentData = FXCollections.observableArrayList();

    // Attendance
    @FXML
    private TableView<AttendanceRecord> attendanceTable;
    @FXML
    private TableColumn<AttendanceRecord, String> attMemberColumn;
    @FXML
    private TableColumn<AttendanceRecord, String> attTypeColumn;
    @FXML
    private TableColumn<AttendanceRecord, String> attTimeColumn;
    @FXML
    private ComboBox<String> attMemberCombo;
    @FXML
    private ChoiceBox<String> attTypeChoice;

    private final ObservableList<AttendanceRecord> attendanceData = FXCollections.observableArrayList();

    // Progress
    @FXML
    private TableView<ProgressEntry> progressTable;
    @FXML
    private TableColumn<ProgressEntry, String> progMemberColumn;
    @FXML
    private TableColumn<ProgressEntry, String> progDateColumn;
    @FXML
    private TableColumn<ProgressEntry, Number> progWeightColumn;
    @FXML
    private TableColumn<ProgressEntry, Number> progBodyFatColumn;
    @FXML
    private TableColumn<ProgressEntry, String> progNoteColumn;
    @FXML
    private ComboBox<String> progMemberCombo;
    @FXML
    private DatePicker progDatePicker;
    @FXML
    private TextField progWeightField;
    @FXML
    private TextField progBodyFatField;
    @FXML
    private TextField progNoteField;

    private final ObservableList<ProgressEntry> progressData = FXCollections.observableArrayList();

    // Classes / Scheduling
    @FXML
    private TableView<ClassSession> classTable;
    @FXML
    private TableColumn<ClassSession, String> classNameColumn;
    @FXML
    private TableColumn<ClassSession, String> classInstructorColumn;
    @FXML
    private TableColumn<ClassSession, String> classDateColumn;
    @FXML
    private TableColumn<ClassSession, String> classTimeColumn;
    @FXML
    private TableColumn<ClassSession, Number> classCapacityColumn;
    @FXML
    private TableColumn<ClassSession, Number> classEnrolledColumn;
    @FXML
    private TextField classNameField;
    @FXML
    private TextField classInstructorField;
    @FXML
    private DatePicker classDatePicker;
    @FXML
    private TextField classTimeField;
    @FXML
    private TextField classCapacityField;

    private final ObservableList<ClassSession> classData = FXCollections.observableArrayList();

    // Notifications
    @FXML
    private TableView<NotificationEntry> notificationTable;
    @FXML
    private TableColumn<NotificationEntry, String> notifTypeColumn;
    @FXML
    private TableColumn<NotificationEntry, String> notifMessageColumn;
    @FXML
    private TableColumn<NotificationEntry, String> notifDateColumn;
    @FXML
    private TableColumn<NotificationEntry, String> notifStatusColumn;
    @FXML
    private ChoiceBox<String> notifTypeChoice;
    @FXML
    private TextField notifMessageField;
    @FXML
    private ChoiceBox<String> notifStatusChoice;

    private final ObservableList<NotificationEntry> notificationData = FXCollections.observableArrayList();

    // Admin
    @FXML
    private Label roleLabel;
    @FXML
    private Label userInfoLabel;
    @FXML
    private Button exportButton;
    @FXML
    private Button seedButton;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private GymRepository repository;

    @FXML
    private void initialize() {
        setupMemberTab();
        setupPaymentTab();
        setupAttendanceTab();
        setupProgressTab();
        setupClassTab();
        setupNotificationTab();
        try {
            repository = new GymRepository();
            loadFromDatabase();
            if (memberData.isEmpty()) {
                repository.seedDemoData();
                loadFromDatabase();
            }
        } catch (Exception e) {
            showAlert("Database Error", "Could not initialize database: " + e.getMessage());
        }
    }

    // Member handling
    private void setupMemberTab() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        membershipColumn.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        memberTable.setItems(memberData);
    }

    @FXML
    private void handleAddMember() {
        String name = nameField.getText();
        String email = emailField.getText();
        String membership = membershipField.getText();

        if (name.isBlank() || email.isBlank() || membership.isBlank()) {
            showAlert("Validation Error", "All member fields are required.");
            return;
        }

        try {
            repository.addMemberWithDefaults(new Member(name, email, membership));
            loadFromDatabase();
            clearMemberForm();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add member: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveMember() {
        Member selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                repository.deleteMember(selected.getId());
                memberData.remove(selected);
                refreshMemberPickers();
            } catch (SQLException e) {
                showAlert("Database Error", "Could not remove member: " + e.getMessage());
            }
        } else {
            showAlert("No Selection", "Please select a member to remove.");
        }
    }

    private void clearMemberForm() {
        nameField.clear();
        emailField.clear();
        membershipField.clear();
    }

    // Payments handling
    private void setupPaymentTab() {
        payMemberColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMemberName()));
        payAmountColumn.setCellValueFactory(cd -> new SimpleStringProperty(String.format("$%.2f", cd.getValue().getAmount())));
        payStatusColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus()));
        payDateColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate().format(dateFormatter)));
        payNoteColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNote()));
        paymentTable.setItems(paymentData);

        payStatusChoice.setItems(FXCollections.observableArrayList("Paid", "Pending", "Overdue"));
        payStatusChoice.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddPayment() {
        String member = payMemberCombo.getValue();
        String amountText = payAmountField.getText();
        LocalDate date = payDatePicker.getValue() != null ? payDatePicker.getValue() : LocalDate.now();
        String status = payStatusChoice.getValue() != null ? payStatusChoice.getValue() : "Pending";
        String note = payNoteField.getText() == null ? "" : payNoteField.getText();

        if (member == null || member.isBlank()) {
            showAlert("Validation Error", "Please choose a member for the payment.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Amount must be a valid number.");
            return;
        }

        try {
            Payment saved = repository.addPayment(new Payment(member, amount, date, status, note));
            paymentData.add(saved);
            payAmountField.clear();
            payNoteField.clear();
            payDatePicker.setValue(null);
            payStatusChoice.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add payment: " + e.getMessage());
        }
    }

    // Attendance handling
    private void setupAttendanceTab() {
        attMemberColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMemberName()));
        attTypeColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getType()));
        attTimeColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTime().format(dateTimeFormatter)));
        attendanceTable.setItems(attendanceData);

        attTypeChoice.setItems(FXCollections.observableArrayList("Check-in", "Check-out"));
        attTypeChoice.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddAttendance() {
        String member = attMemberCombo.getValue();
        String type = attTypeChoice.getValue();

        if (member == null || member.isBlank()) {
            showAlert("Validation Error", "Please choose a member to log attendance.");
            return;
        }

        try {
            AttendanceRecord saved = repository.addAttendance(new AttendanceRecord(member, type, LocalDateTime.now()));
            attendanceData.add(0, saved);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add attendance: " + e.getMessage());
        }
    }

    // Progress handling
    private void setupProgressTab() {
        progMemberColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getMemberName()));
        progDateColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate().format(dateFormatter)));
        progWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        progBodyFatColumn.setCellValueFactory(new PropertyValueFactory<>("bodyFat"));
        progNoteColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNote()));
        progressTable.setItems(progressData);
    }

    @FXML
    private void handleAddProgress() {
        String member = progMemberCombo.getValue();
        LocalDate date = progDatePicker.getValue() != null ? progDatePicker.getValue() : LocalDate.now();
        String weightText = progWeightField.getText();
        String bodyFatText = progBodyFatField.getText();
        String note = progNoteField.getText() == null ? "" : progNoteField.getText();

        if (member == null || member.isBlank()) {
            showAlert("Validation Error", "Please choose a member to record progress.");
            return;
        }

        double weight;
        double bodyFat;
        try {
            weight = Double.parseDouble(weightText);
            bodyFat = Double.parseDouble(bodyFatText);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Weight and body fat must be valid numbers.");
            return;
        }

        try {
            ProgressEntry saved = repository.addProgress(new ProgressEntry(member, date, weight, bodyFat, note));
            progressData.add(0, saved);
            progDatePicker.setValue(null);
            progWeightField.clear();
            progBodyFatField.clear();
            progNoteField.clear();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add progress entry: " + e.getMessage());
        }
    }

    // Classes handling
    private void setupClassTab() {
        classNameColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        classInstructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        classDateColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate().format(dateFormatter)));
        classTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        classCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        classEnrolledColumn.setCellValueFactory(new PropertyValueFactory<>("enrolled"));
        classTable.setItems(classData);
    }

    @FXML
    private void handleAddClass() {
        String className = classNameField.getText();
        String instructor = classInstructorField.getText();
        LocalDate date = classDatePicker.getValue();
        String time = classTimeField.getText();
        String capacityText = classCapacityField.getText();

        if (className.isBlank() || instructor.isBlank() || date == null || time.isBlank() || capacityText.isBlank()) {
            showAlert("Validation Error", "All class fields are required.");
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityText);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Capacity must be a number.");
            return;
        }

        try {
            ClassSession saved = repository.addClass(new ClassSession(className, instructor, date, time, capacity, 0));
            classData.add(saved);
            classNameField.clear();
            classInstructorField.clear();
            classDatePicker.setValue(null);
            classTimeField.clear();
            classCapacityField.clear();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add class: " + e.getMessage());
        }
    }

    // Notifications handling
    private void setupNotificationTab() {
        notifTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        notifMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        notifDateColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDate().format(dateFormatter)));
        notifStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        notificationTable.setItems(notificationData);

        notifTypeChoice.setItems(FXCollections.observableArrayList("Renewal", "Class Reminder", "Alert"));
        notifStatusChoice.setItems(FXCollections.observableArrayList("Pending", "Sent"));
        notifTypeChoice.getSelectionModel().selectFirst();
        notifStatusChoice.getSelectionModel().selectFirst();
    }

    private void loadFromDatabase() {
        if (repository == null) {
            return;
        }
        try {
            memberData.setAll(repository.loadMembers());
            paymentData.setAll(repository.loadPayments());
            attendanceData.setAll(repository.loadAttendance());
            progressData.setAll(repository.loadProgress());
            classData.setAll(repository.loadClasses());
            notificationData.setAll(repository.loadNotifications());
            refreshMemberPickers();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load data: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddNotification() {
        String type = notifTypeChoice.getValue();
        String message = notifMessageField.getText();
        String status = notifStatusChoice.getValue();

        if (message == null || message.isBlank()) {
            showAlert("Validation Error", "Notification message cannot be empty.");
            return;
        }

        try {
            NotificationEntry saved = repository.addNotification(new NotificationEntry(type, message, LocalDate.now(), status));
            notificationData.add(0, saved);
            notifMessageField.clear();
            notifTypeChoice.getSelectionModel().selectFirst();
            notifStatusChoice.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add notification: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportMembers() {
        try {
            Path targetDir = Path.of("target");
            Files.createDirectories(targetDir);
            Path csv = targetDir.resolve("members-export.csv");
            List<String> lines = memberData.stream()
                    .map(m -> String.join(",", m.getName(), m.getEmail(), m.getMembershipType()))
                    .collect(Collectors.toList());
            lines.add(0, "Name,Email,Membership");
            Files.write(csv, lines);
            showAlert("Export Successful", "Members exported to " + csv.toAbsolutePath());
        } catch (Exception e) {
            showAlert("Export Failed", "Could not export members: " + e.getMessage());
        }
    }

    public void setCurrentUser(String username, String role) {
        roleLabel.setText(role);
        userInfoLabel.setText("Logged in as: " + username);
        boolean isAdmin = "Admin".equalsIgnoreCase(role);
        exportButton.setDisable(!isAdmin);
        seedButton.setDisable(!isAdmin);
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/gymapp/views/login-view.fxml"));
            Stage stage = (Stage) roleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            showAlert("Logout Failed", "Could not return to login screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleSeedDemoData() {
        try {
            repository.seedDemoData();
            loadFromDatabase();
        } catch (Exception e) {
            showAlert("Database Error", "Could not seed demo data: " + e.getMessage());
        }
    }

    private void refreshMemberPickers() {
        memberNames.setAll(memberData.stream().map(Member::getName).toList());
        payMemberCombo.setItems(memberNames);
        attMemberCombo.setItems(memberNames);
        progMemberCombo.setItems(memberNames);

        if (!memberNames.isEmpty()) {
            payMemberCombo.getSelectionModel().selectFirst();
            attMemberCombo.getSelectionModel().selectFirst();
            progMemberCombo.getSelectionModel().selectFirst();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
