package model;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class StatsDAO {

    // ── Dashboard stats ───────────────────────────────────────
    public int    getTotalEmployees()    { return queryInt("SELECT COUNT(*) FROM employees"); }
    public int    getPendingLeaveCount() { return queryInt("SELECT COUNT(*) FROM leave_requests WHERE status='Pending'"); }
    public int    getDepartmentCount()   { return queryInt("SELECT COUNT(DISTINCT department) FROM employees WHERE department IS NOT NULL"); }
    public int    getApprovedLeaveCount(){ return queryInt("SELECT COUNT(*) FROM leave_requests WHERE status='Approved'"); }
    public int    getRejectedLeaveCount(){ return queryInt("SELECT COUNT(*) FROM leave_requests WHERE status='Rejected'"); }

    public double getAverageSalary() {
        try (Connection c = DBConnection.getConnection();
             Statement st  = c.createStatement();
             ResultSet rs  = st.executeQuery("SELECT AVG(salary) FROM employees")) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Integer> getDepartmentDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT department, COUNT(*) AS cnt FROM employees " +
                     "WHERE department IS NOT NULL GROUP BY department";
        try (Connection c = DBConnection.getConnection();
             Statement st  = c.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("department"), rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    // ── Leave requests (all — for admin) ─────────────────────
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name " +
                     "FROM leave_requests lr JOIN employees e ON lr.employee_id=e.id " +
                     "ORDER BY lr.applied_on DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st  = c.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapLeave(rs, true));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateLeaveStatus(int leaveId, String status, String adminNote) {
        String sql = "UPDATE leave_requests SET status=?, admin_note=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, adminNote);
            ps.setInt(3, leaveId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Per-employee stats ────────────────────────────────────
    public int getTotalAttendance(int empId) {
        return queryIntParam("SELECT COUNT(*) FROM attendance WHERE employee_id=?", empId);
    }

    public int getApprovedLeaves(int empId) {
        return queryIntParam("SELECT COUNT(*) FROM leave_requests WHERE employee_id=? AND status='Approved'", empId);
    }

    public int getPendingLeaves(int empId) {
        return queryIntParam("SELECT COUNT(*) FROM leave_requests WHERE employee_id=? AND status='Pending'", empId);
    }

    public int[] getMonthlyAttendance(int empId) {
        int[] counts = new int[6];
        String sql = "SELECT MONTH(work_date) AS m, COUNT(*) AS cnt FROM attendance " +
                     "WHERE employee_id=? AND YEAR(work_date)=YEAR(CURDATE()) " +
                     "AND MONTH(work_date) BETWEEN 1 AND 6 GROUP BY m";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int m = rs.getInt("m");
                if (m >= 1 && m <= 6) counts[m-1] = rs.getInt("cnt");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return counts;
    }

    public List<LeaveRequest> getEmployeeLeaveRequests(int empId) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, '' AS emp_name FROM leave_requests lr " +
                     "WHERE lr.employee_id=? ORDER BY lr.applied_on DESC LIMIT 20";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapLeave(rs, false));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Attendance: check-in / check-out ─────────────────────

    /** Returns today's attendance record for the employee, or null. */
    public Attendance getTodayAttendance(int empId) {
        String sql = "SELECT a.*, '' AS emp_name FROM attendance a " +
                     "WHERE a.employee_id=? AND a.work_date=CURDATE()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapAttendance(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Check-in: inserts today's row with current time as check_in.
     * Returns false if already checked in (UNIQUE constraint on employee+date).
     */
    public boolean checkIn(int empId) {
        String sql = "INSERT IGNORE INTO attendance (employee_id, work_date, check_in) " +
                     "VALUES (?, CURDATE(), CURTIME())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Check-out: updates today's row with current time as check_out.
     * Returns false if not yet checked in.
     */
    public boolean checkOut(int empId) {
        String sql = "UPDATE attendance SET check_out=CURTIME() " +
                     "WHERE employee_id=? AND work_date=CURDATE() AND check_out IS NULL";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Full attendance history for an employee. */
    public List<Attendance> getAttendanceHistory(int empId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, '' AS emp_name FROM attendance a " +
                     "WHERE a.employee_id=? ORDER BY a.work_date DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapAttendance(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** All attendance (for admin view). */
    public List<Attendance> getAllAttendance() {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, CONCAT(e.first_name,' ',e.last_name) AS emp_name " +
                     "FROM attendance a JOIN employees e ON a.employee_id=e.id " +
                     "ORDER BY a.work_date DESC";
        try (Connection c = DBConnection.getConnection();
             Statement st  = c.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapAttendance(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Apply for leave (includes reason). */
    public boolean applyLeave(int empId, String leaveType, LocalDate start,
                              LocalDate end, String reason) {
        String sql = "INSERT INTO leave_requests (employee_id,leave_type,start_date,end_date,reason) " +
                     "VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, leaveType);
            ps.setDate(3, Date.valueOf(start));
            ps.setDate(4, Date.valueOf(end));
            ps.setString(5, reason);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    public boolean hasAttendance(int employeeId, java.time.LocalDate date) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND work_date = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Find employee by user_id. */
    public Employee getEmployeeByUserId(int userId) {
        return new EmployeeDAO().findByUserId(userId);
    }

    // ── Helpers ───────────────────────────────────────────────
    private int queryInt(String sql) {
        try (Connection c = DBConnection.getConnection();
             Statement st  = c.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int queryIntParam(String sql, int param) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private LeaveRequest mapLeave(ResultSet rs, boolean includeEmpName) throws SQLException {
        return new LeaveRequest(
                rs.getInt("id"),
                rs.getInt("employee_id"),
                includeEmpName ? rs.getString("emp_name") : "",
                rs.getString("leave_type"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate(),
                rs.getString("reason"),
                rs.getString("status"),
                rs.getString("admin_note"));
    }

    private Attendance mapAttendance(ResultSet rs) throws SQLException {
        Time ci = rs.getTime("check_in");
        Time co = rs.getTime("check_out");
        return new Attendance(
                rs.getInt("id"),
                rs.getInt("employee_id"),
                rs.getString("emp_name"),
                rs.getDate("work_date").toLocalDate(),
                ci != null ? ci.toLocalTime() : null,
                co != null ? co.toLocalTime() : null);
    }
}
