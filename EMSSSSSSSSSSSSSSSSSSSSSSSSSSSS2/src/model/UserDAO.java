package model;

import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO {

    /** Authenticate — returns null if credentials don't match. */
    public User authenticate(String username, String password, String role) {
        String sql = "SELECT id,username,role,email FROM users " +
                     "WHERE username=? AND password=? AND role=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return null;
    }

    /** Find by username only (for OTP flow). */
    public User findByUsername(String username) {
        String sql = "SELECT id,username,role,email FROM users WHERE username=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Check if username already exists. */
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    /** Create a brand-new user; returns generated id or -1 on failure. */
    public int createUser(String username, String password, String role, String email) {
        String sql = "INSERT INTO users (username,password,role,email) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.setString(4, email);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    /** Save OTP and its 10-minute expiry for admin reset. */
    public boolean saveOTP(int userId, String otp, LocalDateTime expiry) {
        String sql = "UPDATE users SET otp=?, otp_expiry=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setTimestamp(2, Timestamp.valueOf(expiry));
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Validate OTP: returns the user if the OTP matches and hasn't expired.
     * Uses DB-side time comparison for reliability.
     */
    public User validateOTP(String username, String otp) {
        String sql = "SELECT id,username,role,email,otp,otp_expiry FROM users WHERE username=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            String storedOtp   = rs.getString("otp");
            Timestamp expiryTs = rs.getTimestamp("otp_expiry");

            if (storedOtp == null || expiryTs == null) return null;

            if (!storedOtp.trim().equals(otp.trim())) return null;

            LocalDateTime expiry = expiryTs.toLocalDateTime();
            if (LocalDateTime.now().isAfter(expiry)) return null;

            return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Set a new password (plain text — hash in production). */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=?, otp=NULL, otp_expiry=NULL WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Find user by their id. */
    public User findById(int id) {
        String sql = "SELECT id,username,role,email FROM users WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Update username for a user.
     * Returns false if the new username is already taken by someone else.
     */
    public boolean updateUsername(int userId, String newUsername) {
        // Check not taken by another user
        User existing = findByUsername(newUsername);
        if (existing != null && existing.getId() != userId) return false;
        String sql = "UPDATE users SET username=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"),
                        rs.getString("role"), rs.getString("email"));
    }
}
