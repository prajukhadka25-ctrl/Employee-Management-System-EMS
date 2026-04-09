package controller;

import javafx.scene.control.*;
import model.*;
import view.EmployeeDashboardView;
import view.LoginView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CONTROLLER LAYER — Employee Dashboard.
 * Wires all sidebar buttons, quick-action buttons, attendance check-in/out,
 * leave application with reason, and profile editing.
 * No direct Stage calls — all delegated to EmployeeDashboardView.
 */
public class EmployeeDashboardController {

    private final EmployeeDashboardView view;
    private final StatsDAO              statsDAO;
    private final EmployeeDAO           employeeDAO;
    private final User                  currentUser;
    private       Employee              employee;

    public EmployeeDashboardController(EmployeeDashboardView view, User user) {
        this.view        = view;
        this.currentUser = user;
        this.statsDAO    = new StatsDAO();
        this.employeeDAO = new EmployeeDAO();
        this.employee    = statsDAO.getEmployeeByUserId(user.getId());

        loadDashboard();
        attachHandlers();
        view.setOnCloseRequest(this::handleLogout);
    }

    // ── Load dashboard stats ──────────────────────────────────
    private void loadDashboard() {
        if (employee == null) return;
        int eid = employee.getId();
        view.setTotalAttendance(statsDAO.getTotalAttendance(eid));
        view.setApprovedLeaves(statsDAO.getApprovedLeaves(eid));
        view.setPendingLeaves(statsDAO.getPendingLeaves(eid));
        view.drawBarChart(statsDAO.getMonthlyAttendance(eid));
        view.setLeaveRequests(statsDAO.getEmployeeLeaveRequests(eid));
    }

    // ── Wire all buttons ──────────────────────────────────────
    private void attachHandlers() {

        // ── Sidebar: Dashboard ────────────────────────────────
        view.getBtnDashboard().setOnAction(e -> {
            view.setNavActive(view.getBtnDashboard());
            String name = employee != null ? employee.getFullName() : currentUser.getUsername();
            view.showCenter(view.buildDashboardContent(name));
            loadDashboard();
            reWireQuickActions();
        });

        // ── Sidebar: My Profile ───────────────────────────────
        view.getBtnProfile().setOnAction(e -> {
            view.setNavActive(view.getBtnProfile());
            openProfile();
        });

        // ── Sidebar: Attendance ───────────────────────────────
        view.getBtnAttendance().setOnAction(e -> {
            view.setNavActive(view.getBtnAttendance());
            openAttendancePage();
        });

        // ── Sidebar: Apply Leave ──────────────────────────────
        view.getBtnApplyLeave().setOnAction(e -> {
            view.setNavActive(view.getBtnApplyLeave());
            openApplyLeavePage();
        });

        // ── Logout ────────────────────────────────────────────
        view.getBtnLogout().setOnAction(e -> handleLogout());

        reWireQuickActions();
    }

    /**
     * Quick-action buttons are inside the dashboard content node which gets
     * rebuilt on each navigation. Call this after every showCenter(dashboard).
     */
    private void reWireQuickActions() {
        if (view.getBtnMarkAttendance() != null)
            view.getBtnMarkAttendance().setOnAction(e -> openAttendancePage());
        if (view.getBtnApplyLeaveAction() != null)
            view.getBtnApplyLeaveAction().setOnAction(e -> openApplyLeavePage());
        if (view.getBtnViewLeaveStatus() != null)
            view.getBtnViewLeaveStatus().setOnAction(e -> openApplyLeavePage());
    }

    // ── Profile (view mode) ───────────────────────────────────
    private void openProfile() {
        String name = employee != null ? employee.getFullName() : currentUser.getUsername();
        view.showCenter(view.buildProfilePage(name));
        view.populateProfile(employee);
        if (view.getBtnEditProfile() != null)
            view.getBtnEditProfile().setOnAction(e -> openEditProfilePage());
    }

    // ── Inline Edit Profile page ──────────────────────────────
    @SuppressWarnings("unchecked")
    private void openEditProfilePage() {
        if (employee == null) { info("No employee record linked to your account."); return; }

        Object[] parts = view.buildEditProfilePage(employee);
        view.showCenter((javafx.scene.Node) parts[0]);

        javafx.scene.control.TextField     firstNameF = (javafx.scene.control.TextField)     parts[1];
        javafx.scene.control.TextField     lastNameF  = (javafx.scene.control.TextField)     parts[2];
        javafx.scene.control.TextField     emailF     = (javafx.scene.control.TextField)     parts[3];
        javafx.scene.control.Button        saveInfoBtn= (javafx.scene.control.Button)        parts[5];
        javafx.scene.control.Button        cancelBtn  = (javafx.scene.control.Button)        parts[6];
        javafx.scene.control.PasswordField curPwF     = (javafx.scene.control.PasswordField) parts[7];
        javafx.scene.control.PasswordField newPwF     = (javafx.scene.control.PasswordField) parts[8];
        javafx.scene.control.PasswordField confPwF    = (javafx.scene.control.PasswordField) parts[9];
        javafx.scene.control.Button        savePwBtn  = (javafx.scene.control.Button)        parts[10];
        javafx.scene.control.Label         infoStatus = (javafx.scene.control.Label)         parts[11];
        javafx.scene.control.Label         pwStatus   = (javafx.scene.control.Label)         parts[12];

        cancelBtn.setOnAction(e -> openProfile());

        // ---- Save personal information --------------------------
        saveInfoBtn.setOnAction(e -> {
            String firstName = firstNameF.getText().trim();
            String lastName  = lastNameF.getText().trim();
            String email     = emailF.getText().trim();

            if (firstName.isEmpty()) {
                EmployeeDashboardView.showStatus(infoStatus, "First name is required.", false); return;
            }
            if (!firstName.matches("[a-zA-Z]+")) {
                EmployeeDashboardView.showStatus(infoStatus, "First name must contain alphabets only.", false); return;
            }
            if (lastName.isEmpty()) {
                EmployeeDashboardView.showStatus(infoStatus, "Last name is required.", false); return;
            }
            if (!lastName.matches("[a-zA-Z]+")) {
                EmployeeDashboardView.showStatus(infoStatus, "Last name must contain alphabets only.", false); return;
            }
            if (email.isEmpty() || !email.contains("@")) {
                EmployeeDashboardView.showStatus(infoStatus, "Please enter a valid email address.", false); return;
            }

            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setEmail(email);

            if (employeeDAO.updateEmployee(employee)) {
                view.populateProfile(employee);
                EmployeeDashboardView.showStatus(infoStatus,
                    "Profile saved successfully! Your information has been updated.", true);
                loadDashboard();
            } else {
                EmployeeDashboardView.showStatus(infoStatus,
                    "Save failed. The email may already be in use by another account.", false);
                employee = statsDAO.getEmployeeByUserId(currentUser.getId());
            }
        });

        // ---- Change password ------------------------------------
        savePwBtn.setOnAction(e -> {
            String curPw  = curPwF.getText().trim();
            String newPw  = newPwF.getText().trim();
            String confPw = confPwF.getText().trim();

            pwStatus.setVisible(false); pwStatus.setManaged(false);

            if (curPw.isEmpty()) {
                EmployeeDashboardView.showStatus(pwStatus, "Please enter your current password.", false); return;
            }
            if (newPw.isEmpty()) {
                EmployeeDashboardView.showStatus(pwStatus, "Please enter a new password.", false); return;
            }
            if (newPw.length() < 4) {
                EmployeeDashboardView.showStatus(pwStatus, "New password must be at least 4 characters.", false); return;
            }
            if (!newPw.equals(confPw)) {
                EmployeeDashboardView.showStatus(pwStatus, "New passwords do not match. Please try again.", false);
                newPwF.clear(); confPwF.clear(); newPwF.requestFocus(); return;
            }

            model.UserDAO userDAO = new model.UserDAO();
            model.User verified = userDAO.authenticate(
                currentUser.getUsername(), curPw, currentUser.getRole());
            if (verified == null) {
                EmployeeDashboardView.showStatus(pwStatus,
                    "Current password is incorrect. Please try again.", false);
                curPwF.clear(); curPwF.requestFocus(); return;
            }

            boolean saved = userDAO.updatePassword(currentUser.getId(), newPw);
            if (saved) {
                EmployeeDashboardView.showStatus(pwStatus,
                    "Password changed successfully! Use your new password next time you log in.", true);
                curPwF.clear(); newPwF.clear(); confPwF.clear();
            } else {
                EmployeeDashboardView.showStatus(pwStatus,
                    "Failed to save new password. Please try again.", false);
            }
        });
    }

    // ── Attendance ────────────────────────────────────────────
    private void openAttendancePage() {
        if (employee == null) { info("No employee record linked to your account."); return; }
        int eid = employee.getId();
        Attendance today   = statsDAO.getTodayAttendance(eid);
        List<Attendance> history = statsDAO.getAttendanceHistory(eid);

        view.showCenter(view.buildAttendancePage(
            today,
            history,
            () -> {   // onCheckIn
                boolean ok = statsDAO.checkIn(eid);
                if (ok) {
                    info("✅ Checked in at " + java.time.LocalTime.now().withNano(0));
                    loadDashboard();
                    openAttendancePage();
                } else {
                    info("You have already checked in today.");
                }
            },
            () -> {   // onCheckOut
                boolean ok = statsDAO.checkOut(eid);
                if (ok) {
                    info("✅ Checked out at " + java.time.LocalTime.now().withNano(0));
                    loadDashboard();
                    openAttendancePage();
                } else {
                    info("Check-out failed. Make sure you are checked in first.");
                }
            }
        ));
    }

    // ── Apply Leave ───────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void openApplyLeavePage() {
        if (employee == null) { info("No employee record linked to your account."); return; }
        int eid = employee.getId();
        List<LeaveRequest> history = statsDAO.getEmployeeLeaveRequests(eid);

        Object[] parts = view.buildApplyLeavePage(history);
        view.showCenter((javafx.scene.Node) parts[0]);

        ComboBox<String>        typeCombo   = (ComboBox<String>)        parts[1];
        DatePicker              startPicker = (DatePicker)              parts[2];
        DatePicker              endPicker   = (DatePicker)              parts[3];
        TextArea                reasonArea  = (TextArea)                parts[4];
        Button                  submitBtn   = (Button)                  parts[5];
        TableView<LeaveRequest> histTable   = (TableView<LeaveRequest>) parts[6];

        submitBtn.setOnAction(e -> {
            // ── Validation ────────────────────────────────────
            if (typeCombo.getValue() == null) {
                showFormError("Please select a leave type."); return;
            }
            if (startPicker.getValue() == null || endPicker.getValue() == null) {
                showFormError("Please select both start and end dates."); return;
            }
            if (endPicker.getValue().isBefore(startPicker.getValue())) {
                showFormError("End date cannot be before start date."); return;
            }
            if (reasonArea.getText().trim().isEmpty()) {
                showFormError("Please provide a reason for your leave request."); return;
            }

            // ── Block leave if attendance already marked ───────
            LocalDate check = startPicker.getValue();
            while (!check.isAfter(endPicker.getValue())) {
                if (statsDAO.hasAttendance(eid, check)) {
                    showFormError("Cannot apply leave for " + check
                        + " — attendance is already marked for that day.");
                    return;
                }
                check = check.plusDays(1);
            }

            // ── Submit ────────────────────────────────────────
            boolean ok = statsDAO.applyLeave(
                eid,
                typeCombo.getValue(),
                startPicker.getValue(),
                endPicker.getValue(),
                reasonArea.getText().trim()
            );

            if (ok) {
                info("✅ Leave request submitted!\n\n" +
                     "Type   : " + typeCombo.getValue() + "\n" +
                     "From   : " + startPicker.getValue() + "\n" +
                     "To     : " + endPicker.getValue() + "\n" +
                     "Status : Pending");
                typeCombo.setValue(null);
                reasonArea.clear();
                startPicker.setValue(LocalDate.now());
                endPicker.setValue(LocalDate.now());
                histTable.getItems().setAll(statsDAO.getEmployeeLeaveRequests(eid));
                loadDashboard();
            } else {
                showFormError("Failed to submit leave request. Please try again.");
            }
        });
    }

    // ── Logout ────────────────────────────────────────────────
    private void handleLogout() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to logout?", ButtonType.YES, ButtonType.NO);
        c.setTitle("Logout");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                view.close();
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                loginView.show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("EMS"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }

    private void showFormError(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle("Validation Error"); a.setHeaderText(null); a.showAndWait();
    }
}