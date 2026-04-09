package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import model.*;
import view.*;

import java.util.Optional;

/**
 * CONTROLLER LAYER — Admin Dashboard.
 * No direct Stage manipulation — all delegated to AdminDashboardView.
 */
public class AdminDashboardController {

    private final AdminDashboardView           view;
    private final StatsDAO                     statsDAO;
    private final EmployeeDAO                  employeeDAO;
    private final User                         currentUser;
    private final ObservableList<LeaveRequest> leaveData;

    public AdminDashboardController(AdminDashboardView view, User user) {
        this.view        = view;
        this.currentUser = user;
        this.statsDAO    = new StatsDAO();
        this.employeeDAO = new EmployeeDAO();
        this.leaveData   = FXCollections.observableArrayList();

        view.getLeaveTable().setItems(leaveData);
        loadDashboard();
        attachHandlers();
        wireLeaveActions();
        view.setOnCloseRequest(this::handleLogout);
    }

    // ── Load dashboard data ───────────────────────────────────
    private void loadDashboard() {
        view.setTotalEmployees(statsDAO.getTotalEmployees());
        view.setPendingLeaves(statsDAO.getPendingLeaveCount());
        view.setDepartments(statsDAO.getDepartmentCount());
        view.setAvgSalary(statsDAO.getAverageSalary());
        view.setApprovedLeaves(statsDAO.getApprovedLeaveCount());
        view.setRejectedLeaves(statsDAO.getRejectedLeaveCount());
        view.drawPieChart(statsDAO.getDepartmentDistribution());
        refreshLeaveTable();
    }

    private void refreshLeaveTable() {
        leaveData.setAll(statsDAO.getAllLeaveRequests());
        view.setPendingLeaves(statsDAO.getPendingLeaveCount());
        view.setApprovedLeaves(statsDAO.getApprovedLeaveCount());
        view.setRejectedLeaves(statsDAO.getRejectedLeaveCount());
    }

    // ── Approve / Reject ──────────────────────────────────────
    private void wireLeaveActions() {
        view.setOnApprove(lr -> {
            if (!"Pending".equals(lr.getStatus())) {
                info("Only Pending requests can be approved."); return;
            }
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Approve Leave");
            c.setHeaderText("Approve leave for " + lr.getEmployeeName() + "?");
            c.setContentText("Type: " + lr.getLeaveType() + "\nPeriod: " + lr.getDateRange());
            c.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    if (statsDAO.updateLeaveStatus(lr.getId(), "Approved", "Approved by admin")) {
                        info("✅ Leave APPROVED for " + lr.getEmployeeName());
                        refreshLeaveTable();
                    } else info("Update failed. Try again.");
                }
            });
        });

        view.setOnReject(lr -> {
            if (!"Pending".equals(lr.getStatus())) {
                info("Only Pending requests can be rejected."); return;
            }
            TextInputDialog td = new TextInputDialog();
            td.setTitle("Reject Leave");
            td.setHeaderText("Reject leave for " + lr.getEmployeeName() + "?");
            td.setContentText("Reason for rejection (optional):");
            td.getDialogPane().setStyle("-fx-background-color:white;");
            Optional<String> reason = td.showAndWait();
            if (reason.isEmpty()) return;   // cancelled
            String note = reason.get().trim().isEmpty() ? "Rejected by admin" : reason.get().trim();
            if (statsDAO.updateLeaveStatus(lr.getId(), "Rejected", note)) {
                info("❌ Leave REJECTED for " + lr.getEmployeeName());
                refreshLeaveTable();
            } else info("Update failed. Try again.");
        });
    }

    // ── Sidebar navigation ────────────────────────────────────
    private void attachHandlers() {

        // Dashboard home
        view.getBtnDashboard().setOnAction(e -> {
            view.setNavActive(view.getBtnDashboard());
            view.showCenter(view.buildDashboardContent());
            view.getLeaveTable().setItems(leaveData);
            wireLeaveActions();
            loadDashboard();
        });

        // Employee list
        view.getBtnEmployees().setOnAction(e -> {
            view.setNavActive(view.getBtnEmployees());
            openEmployeeList();
        });

        // Add employee
        view.getBtnAddEmployee().setOnAction(e -> {
            view.setNavActive(view.getBtnAddEmployee());
            openAddEmployee();
        });

        // Edit employee
        view.getBtnEditEmployee().setOnAction(e -> {
            view.setNavActive(view.getBtnEditEmployee());
            openEditEmployee();
        });

        // Delete employee
        view.getBtnDeleteEmployee().setOnAction(e -> {
            view.setNavActive(view.getBtnDeleteEmployee());
            openDeleteEmployee();
        });

        // Leave requests
        view.getBtnLeaveRequests().setOnAction(e -> {
            view.setNavActive(view.getBtnLeaveRequests());
            // Show full leave requests page as center content
            view.showCenter(buildLeaveRequestsPage());
        });

        // Attendance overview
        view.getBtnAttendance().setOnAction(e -> {
            view.setNavActive(view.getBtnAttendance());
            view.showCenter(view.buildAttendanceContent(statsDAO.getAllAttendance()));
        });

        // Logout
        view.getBtnLogout().setOnAction(e -> handleLogout());
    }

    // ── Build the full leave-requests page (swapped into center) ─
    private javafx.scene.Node buildLeaveRequestsPage() {
        javafx.scene.layout.VBox main = new javafx.scene.layout.VBox(20);
        main.setPadding(new javafx.geometry.Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        javafx.scene.control.Label title = new javafx.scene.control.Label("Leave Requests");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");
        javafx.scene.control.Label sub = new javafx.scene.control.Label("Review and action all leave requests");
        sub.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");

        // Reuse the leave table already wired in the view
        javafx.scene.layout.VBox tableCard = view.card();
        javafx.scene.control.Label hint = new javafx.scene.control.Label(
            "Click ✔ Approve or ✖ Reject on any Pending row");
        hint.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8; -fx-font-style:italic;");
        refreshLeaveTable();
        view.getLeaveTable().setPrefHeight(540);
        tableCard.getChildren().addAll(hint, view.getLeaveTable());

        main.getChildren().addAll(
            new javafx.scene.layout.VBox(3, title, sub), tableCard);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");
        return scroll;
    }

    // ── Open employee list (hides admin, shows DashboardView) ─
    private void openEmployeeList() {
        view.hide();
        DashboardView empList = new DashboardView(currentUser);
        new DashboardController(empList, currentUser, () -> {
            empList.close();
            loadDashboard();
            view.show();
        });
        empList.show();
    }

    // ── Edit Employee (choose from list, then edit form with Save Changes) ──
    private void openEditEmployee() {
        var employees = employeeDAO.getAllEmployees();
        if (employees.isEmpty()) { info("No employees found."); return; }

        var choices = employees.stream()
            .map(emp -> emp.getId() + " — " + emp.getFullName() + " (" + emp.getEmail() + ")")
            .toList();

        javafx.scene.control.ChoiceDialog<String> dlg =
            new javafx.scene.control.ChoiceDialog<>(choices.get(0), choices);
        dlg.setTitle("Edit Employee");
        dlg.setHeaderText("Select employee to edit:");
        dlg.setContentText("Employee:");
        dlg.getDialogPane().setStyle("-fx-background-color:white;");

        dlg.showAndWait().ifPresent(sel -> {
            int id = Integer.parseInt(sel.split(" — ")[0].trim());
            Employee existing = employees.stream()
                .filter(e -> e.getId() == id).findFirst().orElse(null);
            if (existing == null) { info("Employee not found."); return; }

            EmployeeFormView form = new EmployeeFormView(existing);
            UserDAO userDAO = new UserDAO();
            User u = userDAO.findById(existing.getUserId());
            if (u != null) form.setExistingUsername(u.getUsername());

            form.getSaveButton().setOnAction(ev -> {
                if (!form.validate()) return;

                existing.setFirstName(form.getFirstName());
                existing.setLastName(form.getLastName());
                existing.setEmail(form.getEmail());
                existing.setDepartment(form.getDepartment());
                existing.setPosition(form.getPosition());
                existing.setSalary(form.getSalary());
                existing.setHireDate(form.getHireDate());

                if (!employeeDAO.updateEmployee(existing)) {
                    form.showError("Failed to update employee. Email may already be in use.");
                    return;
                }

                String newUsername = form.getUsername();
                if (!newUsername.isEmpty() && existing.getUserId() > 0) {
                    boolean uOk = userDAO.updateUsername(existing.getUserId(), newUsername);
                    if (!uOk) {
                        form.showError("Employee record saved, but username \""
                            + newUsername + "\" is already taken by another account.");
                        loadDashboard();
                        return;
                    }
                }
                loadDashboard();
                form.showSuccess("✅ Saved successfully!\n"
                    + existing.getFullName() + "'s record has been updated.");
            });
            form.show();
        });
    }

    // ── Add Employee (modal form, success shown inside form) ──
    private void openAddEmployee() {
        EmployeeFormView form = new EmployeeFormView(null);
        UserDAO userDAO = new UserDAO();

        form.getSaveButton().setOnAction(e -> {
            if (!form.validate()) return;
            String username = form.getUsername();
            String password = form.getPassword();
            if (userDAO.usernameExists(username)) {
                form.showError("Username '" + username + "' already exists. Please choose another."); return;
            }
            int userId = userDAO.createUser(username, password, "employee", form.getEmail());
            if (userId < 0) { form.showError("Failed to create user account."); return; }
            Employee emp = new Employee(
                form.getFirstName(), form.getLastName(), form.getEmail(),
                form.getDepartment(), form.getPosition(),
                form.getSalary(), form.getHireDate());
            int eid = employeeDAO.addEmployeeWithUserId(emp, userId);
            if (eid > 0) {
                loadDashboard();
                // Green success inside form -- form stays open for another entry
                form.showSuccess("Employee \"" + form.getFirstName() + " " + form.getLastName()
                    + "\" added successfully!\n"
                    + "Username: " + username + "  |  Password: " + password
                    + "\n\nForm cleared. You can add another employee or close this window.");
            } else {
                form.showError("Failed to add employee record. Please try again.");
            }
        });
        form.show();
    }

    // ── Delete employee ───────────────────────────────────────
    private void openDeleteEmployee() {
        var employees = employeeDAO.getAllEmployees();
        if (employees.isEmpty()) { info("No employees found."); return; }

        var choices = employees.stream()
            .map(emp -> emp.getId() + " — " + emp.getFullName() + " (" + emp.getEmail() + ")")
            .toList();

        javafx.scene.control.ChoiceDialog<String> dlg =
            new javafx.scene.control.ChoiceDialog<>(choices.get(0), choices);
        dlg.setTitle("Delete Employee");
        dlg.setHeaderText("Select employee to delete:");
        dlg.setContentText("Employee:");
        dlg.getDialogPane().setStyle("-fx-background-color:white;");

        dlg.showAndWait().ifPresent(sel -> {
            int id = Integer.parseInt(sel.split(" — ")[0].trim());
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + sel + "\"?\nThis cannot be undone.",
                ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Confirm Delete");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    employeeDAO.deleteEmployee(id);
                    loadDashboard();
                    info("Employee deleted.");
                }
            });
        });
    }

    // ── Logout ────────────────────────────────────────────────
    private void handleLogout() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to logout?", ButtonType.YES, ButtonType.NO);
        c.setTitle("Logout"); c.setHeaderText("Confirm Logout");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                view.close();
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                loginView.show();
            }
        });
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("EMS"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}
