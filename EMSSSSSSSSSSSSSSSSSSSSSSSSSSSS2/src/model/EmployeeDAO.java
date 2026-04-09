package model;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st  = c.createStatement();
             ResultSet rs  = st.executeQuery("SELECT * FROM employees ORDER BY id")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Employee> search(String keyword) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE first_name LIKE ? OR last_name LIKE ? " +
                     "OR email LIKE ? OR department LIKE ? OR position LIKE ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Add employee with user_id link; returns generated employee id or -1. */
    public int addEmployeeWithUserId(Employee emp, int userId) {
        String sql = "INSERT INTO employees " +
                     "(user_id,first_name,last_name,email,department,position,salary,hire_date) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            setParams(ps, emp, 2);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    /** Original add (no user_id); kept for backward compatibility. */
    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (first_name,last_name,email,department,position,salary,hire_date) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, emp, 1);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employees SET first_name=?,last_name=?,email=?,department=?," +
                     "position=?,salary=?,hire_date=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, emp, 1);
            ps.setInt(8, emp.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteEmployee(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM employees WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Employee findByUserId(int userId) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM employees WHERE user_id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Helpers ──────────────────────────────────────────────
    /** startCol is 1-based column index for the first employee field param. */
    private void setParams(PreparedStatement ps, Employee e, int startCol) throws SQLException {
        ps.setString(startCol,     e.getFirstName());
        ps.setString(startCol + 1, e.getLastName());
        ps.setString(startCol + 2, e.getEmail());
        ps.setString(startCol + 3, e.getDepartment());
        ps.setString(startCol + 4, e.getPosition());
        ps.setBigDecimal(startCol + 5, e.getSalary());
        ps.setDate(startCol + 6,
                   e.getHireDate() != null ? Date.valueOf(e.getHireDate()) : null);
    }

    Employee map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("hire_date");
        Employee e = new Employee(rs.getInt("id"),
                rs.getString("first_name"), rs.getString("last_name"),
                rs.getString("email"),      rs.getString("department"),
                rs.getString("position"),   rs.getBigDecimal("salary"),
                d != null ? d.toLocalDate() : null);
        // user_id may not be present in all queries -- ignore if missing
        try { e.setUserId(rs.getInt("user_id")); } catch (Exception ignored) {}
        return e;
    }
}
