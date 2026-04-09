package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Employee;
import model.EmployeeDAO;
import model.User;
import view.DashboardView;
import view.EmployeeFormView;
import view.LoginView;

import java.util.Optional;

/**
 * CONTROLLER LAYER — Employee list / CRUD table.
 * Uses an onBack Runnable so it never holds a reference to AdminDashboardView's Stage.
 */
public class DashboardController {

    private final DashboardView            view;
    private final EmployeeDAO              dao;
    private final User                     currentUser;
    private final ObservableList<Employee> data;
    private final Runnable                 onBack;

    public DashboardController(DashboardView view, User user, Runnable onBack) {
        this.view        = view;
        this.currentUser = user;
        this.dao         = new EmployeeDAO();
        this.data        = FXCollections.observableArrayList();
        this.onBack      = onBack;

        view.getTable().setItems(data);
        loadAll();
        attachHandlers();
        view.setOnCloseRequest(onBack);
    }

    // ── Load ──────────────────────────────────────────────────
    private void loadAll() {
        data.setAll(dao.getAllEmployees());
        view.setStatusText("Total employees: " + data.size());
    }

    // ── Handlers ──────────────────────────────────────────────
    private void attachHandlers() {

        view.getBackButton().setOnAction(e -> { if (onBack != null) onBack.run(); });

        view.getSearchField().textProperty().addListener((obs, o, n) -> {
            if (n == null || n.isBlank()) data.setAll(dao.getAllEmployees());
            else                          data.setAll(dao.search(n.trim()));
            view.setStatusText("Results: " + data.size());
        });

        view.getAddButton().setOnAction(e -> openForm(null));

        view.getEditButton().setOnAction(e -> {
            Employee sel = view.getTable().getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select an employee to edit."); return; }
            openForm(sel);
        });

        view.getDeleteButton().setOnAction(e -> {
            Employee sel = view.getTable().getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select an employee to delete."); return; }
            handleDelete(sel);
        });

        view.getRefreshButton().setOnAction(e -> loadAll());

        view.getLogoutButton().setOnAction(e -> handleLogout());

        // Double-click to edit
        view.getTable().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Employee sel = view.getTable().getSelectionModel().getSelectedItem();
                if (sel != null) openForm(sel);
            }
        });
    }

    // ── Add / Edit ────────────────────────────────────────────
    private void openForm(Employee existing) {
        EmployeeFormView form = new EmployeeFormView(existing);
        model.UserDAO userDAO = new model.UserDAO();

        // Pre-fill username in Edit mode
        if (existing != null) {
            model.User u = userDAO.findById(existing.getUserId());
            if (u != null) form.setExistingUsername(u.getUsername());
        }

        form.getSaveButton().setOnAction(e -> {
            if (!form.validate()) return;

            if (existing == null) {
                // ── ADD MODE ──────────────────────────────────
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
                int eid = dao.addEmployeeWithUserId(emp, userId);
                if (eid > 0) {
                    loadAll();
                    // Show green success inside the form (form stays open for another entry)
                    form.showSuccess("Employee \"" + form.getFirstName() + " " + form.getLastName()
                        + "\" added successfully!\nUsername: " + username + "  |  Password: " + password
                        + "\n\nYou can add another employee or close this window.");
                } else {
                    form.showError("Failed to add employee record. Please try again.");
                }

            } else {
                // ── EDIT MODE ─────────────────────────────────
                // Update employee record
                existing.setFirstName(form.getFirstName());
                existing.setLastName(form.getLastName());
                existing.setEmail(form.getEmail());
                existing.setDepartment(form.getDepartment());
                existing.setPosition(form.getPosition());
                existing.setSalary(form.getSalary());
                existing.setHireDate(form.getHireDate());
                if (!dao.updateEmployee(existing)) {
                    form.showError("Failed to update employee. Email may already be in use."); return;
                }
                // Update username if it changed
                String newUsername = form.getUsername();
                if (!newUsername.isEmpty() && existing.getUserId() > 0) {
                    boolean uOk = userDAO.updateUsername(existing.getUserId(), newUsername);
                    if (!uOk) {
                        form.showError("Employee record saved, but username \""
                            + newUsername + "\" is already taken by another account.");
                        loadAll();
                        return;
                    }
                }
                loadAll();
                // Show green success banner inside the form (admin can see it and then close)
                form.showSuccess("Saved successfully!\n"
                    + existing.getFullName() + "'s record has been updated.");
            }
        });
        form.show();
    }

    // ── Delete ────────────────────────────────────────────────
    private void handleDelete(Employee emp) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete " + emp.getFullName() + "?\nThis cannot be undone.",
            ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Confirm Delete");
        Optional<ButtonType> r = c.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            if (!dao.deleteEmployee(emp.getId())) alert("Failed to delete employee.");
            else loadAll();
        }
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

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("EMS"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
