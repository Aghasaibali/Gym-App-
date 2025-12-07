package com.example.gymapp.db;

import com.example.gymapp.models.AttendanceRecord;
import com.example.gymapp.models.ClassSession;
import com.example.gymapp.models.Member;
import com.example.gymapp.models.NotificationEntry;
import com.example.gymapp.models.Payment;
import com.example.gymapp.models.ProgressEntry;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Simple JDBC repository to persist app data in MySQL.
 */
public class GymRepository {

    private final String url;
    private final String user;
    private final String password;

    public GymRepository() throws Exception {
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        }
        this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/gymapp?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC");
        this.user = props.getProperty("db.user", "root");
        this.password = props.getProperty("db.password", "");
        ensureSchema();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void ensureSchema() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        username VARCHAR(100) NOT NULL UNIQUE,
                        email VARCHAR(255) NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(50) NOT NULL
                    )
                    """);
            ensureUserEmailColumn(conn);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS members (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(255) NOT NULL UNIQUE,
                        email VARCHAR(255) NOT NULL,
                        membership_type VARCHAR(100) NOT NULL
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS payments (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        member_name VARCHAR(255) NOT NULL,
                        amount DOUBLE NOT NULL,
                        pay_date DATE NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        note VARCHAR(500)
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS attendance (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        member_name VARCHAR(255) NOT NULL,
                        att_type VARCHAR(50) NOT NULL,
                        att_time DATETIME NOT NULL
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS progress_entries (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        member_name VARCHAR(255) NOT NULL,
                        prog_date DATE NOT NULL,
                        weight DOUBLE NOT NULL,
                        body_fat DOUBLE NOT NULL,
                        note VARCHAR(500)
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS class_sessions (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        class_name VARCHAR(255) NOT NULL,
                        instructor VARCHAR(255) NOT NULL,
                        class_date DATE NOT NULL,
                        class_time VARCHAR(50) NOT NULL,
                        capacity INT NOT NULL,
                        enrolled INT NOT NULL
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS notification_entries (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        notif_type VARCHAR(100) NOT NULL,
                        message VARCHAR(500) NOT NULL,
                        notif_date DATE NOT NULL,
                        status VARCHAR(50) NOT NULL
                    )
                    """);
        }
        ensureDefaultUsers();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private void insertUser(Connection conn, String username, String email, String password, String role) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, email, password_hash, role) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashPassword(password));
            ps.setString(4, role);
            ps.executeUpdate();
        }
    }

    private void ensureDefaultUsers() throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                ensureUserEmailColumn(conn);
                int totalUsers;
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users"); ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    totalUsers = rs.getInt(1);
                }
                if (totalUsers == 0) {
                    insertUser(conn, "owner", "owner@example.af", "owner123", "Admin");
                    insertUser(conn, "trainer", "trainer@example.af", "train123", "User");
                    insertUser(conn, "member", "member@example.af", "member123", "User");
                }
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("""
                            UPDATE users SET email = CONCAT(username, '@example.af')
                            WHERE (email IS NULL OR email = '')
                            """);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void ensureUserEmailColumn(Connection conn) throws SQLException {
        boolean hasEmail = false;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                hasEmail = rs.getInt(1) > 0;
            }
        }
        if (!hasEmail) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN email VARCHAR(255) NOT NULL DEFAULT '' AFTER username");
            }
        }
    }

    public void registerUser(String username, String email, String password) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                insertUser(conn, username, email, password, "User");
                insertMemberWithDefaults(conn, new Member(username, email, "Standard"));
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Member insertMemberWithDefaults(Connection conn, Member member) throws SQLException {
        Member saved = insertMember(conn, member);
        insertDefaultPayment(conn, saved.getName());
        insertDefaultAttendance(conn, saved.getName());
        return saved;
    }

    private Member insertMember(Connection conn, Member member) throws SQLException {
        String sql = "INSERT INTO members (name, email, membership_type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getMembershipType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    member.setId(keys.getInt(1));
                }
            }
        }
        return member;
    }

    private Payment insertDefaultPayment(Connection conn, String memberName) throws SQLException {
        Payment payment = new Payment(memberName, 0.0, LocalDate.now(), "Pending", "Initial payment pending");
        String sql = "INSERT INTO payments (member_name, amount, pay_date, status, note) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, payment.getMemberName());
            ps.setDouble(2, payment.getAmount());
            ps.setDate(3, Date.valueOf(payment.getDate()));
            ps.setString(4, payment.getStatus());
            ps.setString(5, payment.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
        }
        return payment;
    }

    private AttendanceRecord insertDefaultAttendance(Connection conn, String memberName) throws SQLException {
        AttendanceRecord record = new AttendanceRecord(memberName, "Check-in", LocalDateTime.now());
        String sql = "INSERT INTO attendance (member_name, att_type, att_time) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getMemberName());
            ps.setString(2, record.getType());
            ps.setTimestamp(3, Timestamp.valueOf(record.getTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
            }
        }
        return record;
    }

    public String authenticate(String username, String password) throws SQLException {
        String sql = "SELECT password_hash, role FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (storedHash.equals(hashPassword(password))) {
                        return rs.getString("role");
                    }
                }
            }
        }
        return null;
    }

    public List<Member> loadMembers() throws SQLException {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT id, name, email, membership_type FROM members ORDER BY id";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("membership_type")));
            }
        }
        return list;
    }

    public Member addMember(Member member) throws SQLException {
        try (Connection conn = getConnection()) {
            return insertMember(conn, member);
        }
    }

    public Member addMemberWithDefaults(Member member) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                Member saved = insertMemberWithDefaults(conn, member);
                conn.commit();
                return saved;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void deleteMember(int memberId) throws SQLException {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.executeUpdate();
        }
    }

    public List<Payment> loadPayments() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT id, member_name, amount, pay_date, status, note FROM payments ORDER BY id";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Payment(
                        rs.getInt("id"),
                        rs.getString("member_name"),
                        rs.getDouble("amount"),
                        rs.getDate("pay_date").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("note")
                ));
            }
        }
        return list;
    }

    public Payment addPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (member_name, amount, pay_date, status, note) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, payment.getMemberName());
            ps.setDouble(2, payment.getAmount());
            ps.setDate(3, Date.valueOf(payment.getDate()));
            ps.setString(4, payment.getStatus());
            ps.setString(5, payment.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
        }
        return payment;
    }

    public List<AttendanceRecord> loadAttendance() throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT id, member_name, att_type, att_time FROM attendance ORDER BY att_time DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AttendanceRecord(
                        rs.getInt("id"),
                        rs.getString("member_name"),
                        rs.getString("att_type"),
                        rs.getTimestamp("att_time").toLocalDateTime()
                ));
            }
        }
        return list;
    }

    public AttendanceRecord addAttendance(AttendanceRecord record) throws SQLException {
        String sql = "INSERT INTO attendance (member_name, att_type, att_time) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getMemberName());
            ps.setString(2, record.getType());
            ps.setTimestamp(3, Timestamp.valueOf(record.getTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
            }
        }
        return record;
    }

    public List<ProgressEntry> loadProgress() throws SQLException {
        List<ProgressEntry> list = new ArrayList<>();
        String sql = "SELECT id, member_name, prog_date, weight, body_fat, note FROM progress_entries ORDER BY prog_date DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ProgressEntry(
                        rs.getInt("id"),
                        rs.getString("member_name"),
                        rs.getDate("prog_date").toLocalDate(),
                        rs.getDouble("weight"),
                        rs.getDouble("body_fat"),
                        rs.getString("note")
                ));
            }
        }
        return list;
    }

    public ProgressEntry addProgress(ProgressEntry entry) throws SQLException {
        String sql = "INSERT INTO progress_entries (member_name, prog_date, weight, body_fat, note) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entry.getMemberName());
            ps.setDate(2, Date.valueOf(entry.getDate()));
            ps.setDouble(3, entry.getWeight());
            ps.setDouble(4, entry.getBodyFat());
            ps.setString(5, entry.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setId(keys.getInt(1));
                }
            }
        }
        return entry;
    }

    public List<ClassSession> loadClasses() throws SQLException {
        List<ClassSession> list = new ArrayList<>();
        String sql = "SELECT id, class_name, instructor, class_date, class_time, capacity, enrolled FROM class_sessions ORDER BY class_date, class_time";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ClassSession(
                        rs.getInt("id"),
                        rs.getString("class_name"),
                        rs.getString("instructor"),
                        rs.getDate("class_date").toLocalDate(),
                        rs.getString("class_time"),
                        rs.getInt("capacity"),
                        rs.getInt("enrolled")
                ));
            }
        }
        return list;
    }

    public ClassSession addClass(ClassSession session) throws SQLException {
        String sql = "INSERT INTO class_sessions (class_name, instructor, class_date, class_time, capacity, enrolled) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, session.getClassName());
            ps.setString(2, session.getInstructor());
            ps.setDate(3, Date.valueOf(session.getDate()));
            ps.setString(4, session.getTime());
            ps.setInt(5, session.getCapacity());
            ps.setInt(6, session.getEnrolled());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    session.setId(keys.getInt(1));
                }
            }
        }
        return session;
    }

    public List<NotificationEntry> loadNotifications() throws SQLException {
        List<NotificationEntry> list = new ArrayList<>();
        String sql = "SELECT id, notif_type, message, notif_date, status FROM notification_entries ORDER BY notif_date DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NotificationEntry(
                        rs.getInt("id"),
                        rs.getString("notif_type"),
                        rs.getString("message"),
                        rs.getDate("notif_date").toLocalDate(),
                        rs.getString("status")
                ));
            }
        }
        return list;
    }

    public NotificationEntry addNotification(NotificationEntry entry) throws SQLException {
        String sql = "INSERT INTO notification_entries (notif_type, message, notif_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entry.getType());
            ps.setString(2, entry.getMessage());
            ps.setDate(3, Date.valueOf(entry.getDate()));
            ps.setString(4, entry.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setId(keys.getInt(1));
                }
            }
        }
        return entry;
    }

    public void clearAll() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM notification_entries");
            st.executeUpdate("DELETE FROM class_sessions");
            st.executeUpdate("DELETE FROM progress_entries");
            st.executeUpdate("DELETE FROM attendance");
            st.executeUpdate("DELETE FROM payments");
            st.executeUpdate("DELETE FROM members");
        }
    }

    public void seedDemoData() throws SQLException {
        clearAll();
        addMember(new Member("Ali Miry", "ali.miry@example.af", "Monthly"));
        addMember(new Member("Mohammad Rahimi", "mohammad.rahimi@example.af", "Yearly"));
        addMember(new Member("Jawid Noori", "jawid.noori@example.af", "Quarterly"));

        addPayment(new Payment("Ali Miry", 49.99, LocalDate.now().minusDays(2), "Paid", "Renewal"));
        addPayment(new Payment("Mohammad Rahimi", 199.00, LocalDate.now().plusDays(5), "Pending", "Yearly due soon"));

        addAttendance(new AttendanceRecord("Ali Miry", "Check-in", LocalDateTime.now().minusHours(2)));
        addAttendance(new AttendanceRecord("Mohammad Rahimi", "Check-in", LocalDateTime.now().minusHours(1)));

        addProgress(new ProgressEntry("Ali Miry", LocalDate.now().minusDays(7), 72.4, 18.2, "Improving"));
        addProgress(new ProgressEntry("Mohammad Rahimi", LocalDate.now().minusDays(3), 65.0, 22.1, "New goal set"));

        addClass(new ClassSession("HIIT", "Coach Farid", LocalDate.now().plusDays(1), "18:00", 20, 12));
        addClass(new ClassSession("Yoga", "Coach Laila", LocalDate.now().plusDays(2), "09:00", 15, 10));

        addNotification(new NotificationEntry("Renewal", "Mohammad Rahimi membership due in 5 days", LocalDate.now(), "Pending"));
        addNotification(new NotificationEntry("Class Reminder", "HIIT class tomorrow 18:00", LocalDate.now(), "Pending"));
    }
}
