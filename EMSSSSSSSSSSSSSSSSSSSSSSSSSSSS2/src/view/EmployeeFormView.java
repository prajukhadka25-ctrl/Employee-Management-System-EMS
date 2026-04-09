package view;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * VIEW LAYER -- Modal form for Add / Edit employee.
 *
 * Add mode  : employee fields + Login Credentials (username, password,
 *             default-password checkbox) + "Add Employee" button.
 *             After successful add -> green success banner, fields cleared,
 *             form stays open so admin can add another employee.
 *
 * Edit mode : employee fields + Username field (editable) +
 *             "Save Changes" button.
 *             After successful save -> green success banner "Saved successfully!"
 *             shown inside the form. Form stays open so admin can confirm the change.
 */
public class EmployeeFormView {

    private final Stage   stage;
    private final boolean isAdd;

    // -- Employee fields --------------------------------------------------
    private TextField  firstNameField, lastNameField, emailField,
                       departmentField, positionField, salaryField;
    private DatePicker hireDatePicker;

    // -- Credentials ------------------------------------------------------
    private TextField     usernameField;
    private PasswordField passwordField;   // null in Edit mode
    private CheckBox      defaultPwCheck;  // null in Edit mode

    // -- Feedback labels --------------------------------------------------
    private Label errorLabel;
    private Label successLabel;

    // -- Buttons ----------------------------------------------------------
    private Button saveButton, cancelButton;

    public EmployeeFormView(Employee existing) {
        this.isAdd = (existing == null);
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isAdd ? "Add New Employee" : "Edit Employee");
        stage.setResizable(false);
        stage.setScene(buildScene(existing));
    }

    // =====================================================================
    private Scene buildScene(Employee emp) {

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f1f5f9;");

        // Blue header bar
        HBox header = new HBox();
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#1a4fa0;");
        Label hLbl = new Label(isAdd ? "+ Add New Employee" : "Edit Employee");
        hLbl.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:white;");
        header.getChildren().add(hLbl);

        // White card
        VBox card = new VBox(14);
        card.setPadding(new Insets(24, 28, 20, 28));
        card.setStyle("-fx-background-color:white;");

        // Error banner (red)
        errorLabel = new Label();
        errorLabel.setStyle(
            "-fx-background-color:#fef2f2; -fx-border-color:#fecaca;"
            + "-fx-border-radius:8; -fx-background-radius:8;"
            + "-fx-text-fill:#dc2626; -fx-font-size:12px; -fx-padding:8 12;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(520);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Success banner (green)
        successLabel = new Label();
        successLabel.setStyle(
            "-fx-background-color:#f0fdf4; -fx-border-color:#bbf7d0;"
            + "-fx-border-radius:8; -fx-background-radius:8;"
            + "-fx-text-fill:#16a34a; -fx-font-size:12px; -fx-padding:8 12;");
        successLabel.setWrapText(true);
        successLabel.setMaxWidth(520);
        successLabel.setVisible(false);
        successLabel.setManaged(false);

        // ---- Employee fields grid ------------------------------------
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);
        ColumnConstraints c1 = new ColumnConstraints(130);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(130);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2, c3, c4);

        firstNameField  = field("First Name *");
        lastNameField   = field("Last Name *");
        emailField      = field("Email *");
        departmentField = field("e.g. Engineering");
        positionField   = field("e.g. Developer");
        salaryField     = field("e.g. 60000");
        hireDatePicker  = new DatePicker(LocalDate.now());
        hireDatePicker.setStyle(inputStyle());
        hireDatePicker.setMaxWidth(Double.MAX_VALUE);

        if (emp != null) {
            firstNameField.setText(emp.getFirstName());
            lastNameField.setText(emp.getLastName());
            emailField.setText(emp.getEmail());
            departmentField.setText(emp.getDepartment() != null ? emp.getDepartment() : "");
            positionField.setText(emp.getPosition()    != null ? emp.getPosition()    : "");
            if (emp.getSalary() != null) salaryField.setText(emp.getSalary().toPlainString());
            hireDatePicker.setValue(emp.getHireDate());
        }

        grid.add(fLabel("First Name *"), 0, 0); grid.add(firstNameField,  1, 0);
        grid.add(fLabel("Last Name *"),  2, 0); grid.add(lastNameField,   3, 0);
        grid.add(fLabel("Email *"),      0, 1); grid.add(emailField,      1, 1);
        grid.add(fLabel("Department"),   2, 1); grid.add(departmentField, 3, 1);
        grid.add(fLabel("Position"),     0, 2); grid.add(positionField,   1, 2);
        grid.add(fLabel("Salary ($)"),   2, 2); grid.add(salaryField,     3, 2);
        grid.add(fLabel("Hire Date"),    0, 3); grid.add(hireDatePicker,  1, 3);

        card.getChildren().addAll(errorLabel, successLabel, grid);

        // ---- Credentials section ------------------------------------
        Separator credSep = new Separator();
        credSep.setPadding(new Insets(4, 0, 4, 0));

        Label credTitle = new Label("Login Credentials");
        credTitle.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");

        GridPane credGrid = new GridPane();
        credGrid.setHgap(16);
        credGrid.setVgap(14);
        ColumnConstraints cc1 = new ColumnConstraints(130);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setHgrow(Priority.ALWAYS);
        ColumnConstraints cc3 = new ColumnConstraints(130);
        ColumnConstraints cc4 = new ColumnConstraints(); cc4.setHgrow(Priority.ALWAYS);
        credGrid.getColumnConstraints().addAll(cc1, cc2, cc3, cc4);

        usernameField = field("e.g. john.doe");

        if (isAdd) {
            // Add mode: username + password + default-pw checkbox
            passwordField = new PasswordField();
            passwordField.setPromptText("Set password");
            passwordField.setStyle(inputStyle());
            passwordField.setMaxWidth(Double.MAX_VALUE);

            defaultPwCheck = new CheckBox("Use default password  (emp@123)");
            defaultPwCheck.setStyle("-fx-font-size:12px; -fx-text-fill:#475569;");
            defaultPwCheck.setOnAction(e -> {
                if (defaultPwCheck.isSelected()) {
                    passwordField.setText("emp@123");
                    passwordField.setDisable(true);
                } else {
                    passwordField.clear();
                    passwordField.setDisable(false);
                }
            });

            credGrid.add(fLabel("Username *"), 0, 0); credGrid.add(usernameField, 1, 0);
            credGrid.add(fLabel("Password *"), 2, 0); credGrid.add(passwordField, 3, 0);
            credGrid.add(defaultPwCheck, 1, 1, 3, 1);

        } else {
            // Edit mode: username editable, no password field
            Label note = new Label("You can rename the employee login username here.");
            note.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
            credGrid.add(fLabel("Username *"), 0, 0); credGrid.add(usernameField, 1, 0);
            credGrid.add(note, 1, 1, 3, 1);
        }

        card.getChildren().addAll(credSep, credTitle, credGrid);

        // ---- Button row ---------------------------------------------
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(14, 0, 0, 0));

        cancelButton = new Button("Cancel");
        cancelButton.setCursor(Cursor.HAND);
        cancelButton.setStyle(
            "-fx-background-color:#e2e8f0; -fx-text-fill:#475569;"
            + "-fx-font-size:13px; -fx-background-radius:8; -fx-padding:9 22;");
        cancelButton.setOnAction(e -> stage.close());

        // "Add Employee" in Add mode, "Save Changes" in Edit mode
        saveButton = new Button(isAdd ? "Add Employee" : "Save Changes");
        saveButton.setCursor(Cursor.HAND);
        saveButton.setStyle(
            "-fx-background-color:linear-gradient(to right,#2563eb,#1a4fa0);"
            + "-fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold;"
            + "-fx-background-radius:8; -fx-padding:9 22;");

        btnRow.getChildren().addAll(cancelButton, saveButton);
        card.getChildren().add(btnRow);

        root.getChildren().addAll(header, card);
        return new Scene(root, 630, isAdd ? 465 : 480);
    }

    // ---- Helpers --------------------------------------------------------
    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(inputStyle());
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private String inputStyle() {
        return "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0;"
             + "-fx-border-radius:8; -fx-background-radius:8;"
             + "-fx-font-size:12px; -fx-padding:7 10;";
    }

    private Label fLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#334155;");
        l.setAlignment(Pos.CENTER_RIGHT);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    // ---- Validation -----------------------------------------------------
    public boolean validate() {
        hideMessages();
        if (firstNameField.getText().isBlank()) { showError("First name is required."); return false; }
        if (!firstNameField.getText().trim().matches("[a-zA-Z]+")) {
            showError("First name must contain alphabets only."); return false;
        }
        if (lastNameField.getText().isBlank())  { showError("Last name is required.");  return false; }
        if (!lastNameField.getText().trim().matches("[a-zA-Z]+")) {
            showError("Last name must contain alphabets only."); return false;
        }
        if (emailField.getText().isBlank())     { showError("Email is required.");      return false; }
        if (!emailField.getText().contains("@")){ showError("Enter a valid email address."); return false; }
        if (!salaryField.getText().isBlank()) {
            try { new BigDecimal(salaryField.getText()); }
            catch (NumberFormatException e) { showError("Salary must be a valid number."); return false; }
        }
        if (usernameField.getText().isBlank()) { showError("Username is required."); return false; }
        if (!usernameField.getText().trim().matches("[a-zA-Z][a-zA-Z0-9._]*")) {
            showError("Username must start with a letter and contain only letters, numbers, dots, or underscores."); return false;
        }
        if (isAdd && passwordField != null && passwordField.getText().isBlank()) {
            showError("Password is required."); return false;
        }
        return true;
    }
    // ---- Public API -----------------------------------------------------
    public void show()  { stage.showAndWait(); }
    public void close() { stage.close(); }

    /** Pre-fill the username field (called by controller in Edit mode). */
    public void setExistingUsername(String username) {
        if (usernameField != null)
            usernameField.setText(username != null ? username : "");
    }

    public String     getFirstName()  { return firstNameField.getText().trim(); }
    public String     getLastName()   { return lastNameField.getText().trim(); }
    public String     getEmail()      { return emailField.getText().trim(); }
    public String     getDepartment() { return departmentField.getText().trim(); }
    public String     getPosition()   { return positionField.getText().trim(); }
    public LocalDate  getHireDate()   { return hireDatePicker.getValue(); }
    public String     getUsername()   { return usernameField.getText().trim(); }
    public String     getPassword()   {
        return (isAdd && passwordField != null) ? passwordField.getText().trim() : null;
    }
    public BigDecimal getSalary() {
        String s = salaryField.getText().trim();
        return s.isBlank() ? null : new BigDecimal(s);
    }

    public Button getSaveButton()   { return saveButton; }
    public Button getCancelButton() { return cancelButton; }

    /**
     * Show a red error banner inside the form.
     * Hides the success banner.
     */
    public void showError(String msg) {
        successLabel.setVisible(false); successLabel.setManaged(false);
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Show a green success banner inside the form.
     * In Add mode: also clears all fields so the admin can add another employee.
     * In Edit mode: just shows the message -- fields keep their values.
     */
    public void showSuccess(String msg) {
        errorLabel.setVisible(false); errorLabel.setManaged(false);
        successLabel.setText(msg);
        successLabel.setVisible(true);
        successLabel.setManaged(true);

        if (isAdd) {
            // Clear form for next entry
            firstNameField.clear(); lastNameField.clear(); emailField.clear();
            departmentField.clear(); positionField.clear(); salaryField.clear();
            hireDatePicker.setValue(LocalDate.now());
            usernameField.clear();
            if (passwordField  != null) { passwordField.clear(); passwordField.setDisable(false); }
            if (defaultPwCheck != null) defaultPwCheck.setSelected(false);
        }
    }

    private void hideMessages() {
        errorLabel.setVisible(false);   errorLabel.setManaged(false);
        successLabel.setVisible(false); successLabel.setManaged(false);
    }
}
