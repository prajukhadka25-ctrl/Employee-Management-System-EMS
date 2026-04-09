package controller;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Employee;
import model.StatsDAO;
import model.User;
import model.UserDAO;
import view.AdminDashboardView;
import view.EmployeeDashboardView;
import view.LoginView;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CONTROLLER LAYER -- Login screen.
 *
 * Forgot password flows:
 *   Employee -> shows "Contact your administrator" message.
 *   Admin    -> 3-step OTP flow:
 *               Step 1: enter username
 *               Step 2: OTP generated + shown, user enters it (retry on wrong OTP)
 *               Step 3: custom password-change dialog (loops until match or cancel)
 *               -> password saved to DB, success message shown.
 */
public class LoginController {

    private final LoginView view;
    private final UserDAO   userDAO;
    private boolean dashboardOpened = false;

    public LoginController(LoginView view) {
        this.view    = view;
        this.userDAO = new UserDAO();
        attachHandlers();
    }

    // ---- Wire handlers --------------------------------------------------
    private void attachHandlers() {
        view.getLoginButton().setOnAction(e   -> handleLogin());
        view.getPasswordField().setOnAction(e -> handleLogin());
        view.getForgotLink().setOnAction(e    -> handleForgotPassword());
    }

    // ---- Login ----------------------------------------------------------
    private void handleLogin() {
        String username = view.getUsernameField().getText().trim();
        String password = view.getPasswordField().getText().trim();
        String role     = view.getSelectedRole();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            view.showError("Please fill in all fields and select a role.");
            return;
        }

        view.clearError();
        view.setLoading(true);

        try {
            String dbRole = "Admin".equals(role) ? "admin" : "employee";
            User user = userDAO.authenticate(username, password, dbRole);
            view.setLoading(false);

            if (user == null) {
                view.showError("Invalid username, password, or role.");
                return;
            }

            final String fullName;
            if ("admin".equals(user.getRole())) {
                fullName = user.getUsername();
            } else {
                Employee emp = new StatsDAO().getEmployeeByUserId(user.getId());
                fullName = (emp != null) ? emp.getFullName() : user.getUsername();
            }
            showSuccessAndOpen(user, fullName);

        } catch (RuntimeException ex) {
            view.setLoading(false);
            view.showError("Cannot connect to database.\n"
                + "Check your MySQL password in DBConnection.java\n"
                + "Error: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
        }
    }

    // ---- Success alert -> auto-open dashboard after 2 s -----------------
    private void showSuccessAndOpen(User user, String fullName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText("Welcome, " + fullName + "!\n\n"
            + "Role     : " + cap(user.getRole()) + "\n"
            + "Username : " + user.getUsername()  + "\n\n"
            + "Opening dashboard...");
        alert.getButtonTypes().setAll(new ButtonType("Continue"));

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> { alert.close(); openDashboard(user, fullName); });
        alert.setOnHidden(e  -> { pause.stop();   openDashboard(user, fullName); });

        view.close();
        pause.play();
        alert.show();
    }

    private void openDashboard(User user, String fullName) {
        if (dashboardOpened) return;
        dashboardOpened = true;

        if ("admin".equals(user.getRole())) {
            AdminDashboardView v = new AdminDashboardView();
            new AdminDashboardController(v, user);
            v.show();
        } else {
            EmployeeDashboardView v = new EmployeeDashboardView(user, fullName);
            new EmployeeDashboardController(v, user);
            v.show();
        }
    }

    // ---- Forgot Password ------------------------------------------------
    private void handleForgotPassword() {
        String role = view.getSelectedRole();
        if (role == null) {
            view.showError("Please select your role first.");
            return;
        }

        // ---- Employee: contact admin ------------------------------------
        if ("Employee".equals(role)) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Forgot Password");
            a.setHeaderText("Password Reset");
            a.setContentText(
                "Please contact your administrator to reset your password.\n\n"
                + "Your admin can update it from the Employee Management screen.");
            stylePane(a.getDialogPane());
            a.showAndWait();
            return;
        }

        // ---- Admin: OTP flow -------------------------------------------

        // STEP 1: enter admin username
        TextInputDialog ud = new TextInputDialog();
        ud.setTitle("Admin Password Reset");
        ud.setHeaderText("Step 1  --  Enter Admin Username");
        ud.setContentText("Admin username:");
        stylePane(ud.getDialogPane());
        Optional<String> ur = ud.showAndWait();
        if (ur.isEmpty() || ur.get().trim().isEmpty()) return;

        String adminUsername = ur.get().trim();
        User adminUser = userDAO.findByUsername(adminUsername);
        if (adminUser == null || !"admin".equals(adminUser.getRole())) {
            infoAlert("Error", "No admin account found with that username.");
            return;
        }

        // STEP 2: generate OTP and show it
        String otp = String.valueOf(100000 + new java.security.SecureRandom().nextInt(900000));
        userDAO.saveOTP(adminUser.getId(), otp, LocalDateTime.now().plusMinutes(10));

        System.out.println("=== EMS OTP === To: " + adminUser.getEmail()
            + "  OTP: " + otp + " (expires 10 min) ===");

        Alert otpShown = new Alert(Alert.AlertType.INFORMATION);
        otpShown.setTitle("OTP Generated");
        otpShown.setHeaderText("Step 2  --  OTP Sent");
        otpShown.setContentText(
            "A one-time password has been generated.\n\n"
            + "OTP  :  " + otp + "\n"
            + "(In production this would be emailed to: "
            + (adminUser.getEmail() != null ? adminUser.getEmail() : "your registered email") + ")\n\n"
            + "It expires in 10 minutes.\n\nClick OK to enter the OTP.");
        stylePane(otpShown.getDialogPane());
        otpShown.showAndWait();

        // STEP 3: enter OTP -- RETRY LOOP until correct or user cancels
        User verified = null;
        while (verified == null) {
            TextInputDialog otpDlg = new TextInputDialog();
            otpDlg.setTitle("Verify OTP");
            otpDlg.setHeaderText("Step 2  --  Verify OTP");
            otpDlg.setContentText("Enter the 6-digit OTP:");
            stylePane(otpDlg.getDialogPane());

            Optional<String> entered = otpDlg.showAndWait();
            if (entered.isEmpty() || entered.get().trim().isEmpty()) return; // cancelled

            verified = userDAO.validateOTP(adminUsername, entered.get().trim());

            if (verified == null) {
                // Wrong OTP -- error dialog with OK to retry / X to cancel
                Alert wrong = new Alert(Alert.AlertType.ERROR);
                wrong.setTitle("Invalid OTP");
                wrong.setHeaderText("Incorrect or Expired OTP");
                wrong.setContentText(
                    "The OTP you entered is incorrect or has expired.\n\n"
                    + "Click OK to try again, or Cancel to abort.");
                wrong.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
                stylePane(wrong.getDialogPane());
                Optional<ButtonType> choice = wrong.showAndWait();
                if (choice.isEmpty() || choice.get() == ButtonType.CANCEL) return;
                // ButtonType.OK -> loop back and ask OTP again
            }
        }

        // STEP 4: change password -- custom Stage dialog (avoids lookupButton timing issue)
        //         Loop until passwords match or user closes the window.
        showChangePasswordDialog(verified);
    }

    /**
     * Opens a custom password-change Stage (modal).
     * Uses a real Stage + buttons instead of Dialog so that button disabling
     * works correctly without timing/lookupButton issues.
     *
     * The stage loops (stays open) if passwords don't match, and closes only
     * when the password is saved successfully or the user clicks Cancel.
     */
    private void showChangePasswordDialog(User verifiedAdmin) {
        Stage pwStage = new Stage();
        pwStage.initModality(Modality.APPLICATION_MODAL);
        pwStage.setTitle("Set New Password");
        pwStage.setResizable(false);

        // -- Layout --
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#f1f5f9;");

        // Blue header
        HBox header = new HBox();
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:#1a4fa0;");
        Label hLbl = new Label("Step 3  --  Set New Password");
        hLbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:white;");
        header.getChildren().add(hLbl);

        // White card
        VBox card = new VBox(12);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle("-fx-background-color:white;");

        String fieldStyle =
            "-fx-font-size:13px; -fx-background-color:#f8fafc;"
            + "-fx-border-color:#e2e8f0; -fx-border-radius:8;"
            + "-fx-background-radius:8; -fx-padding:9 12;";

        // Error banner
        Label errBanner = new Label();
        errBanner.setStyle(
            "-fx-background-color:#fef2f2; -fx-border-color:#fecaca;"
            + "-fx-border-radius:8; -fx-background-radius:8;"
            + "-fx-text-fill:#dc2626; -fx-font-size:12px; -fx-padding:8 12;");
        errBanner.setWrapText(true);
        errBanner.setMaxWidth(360);
        errBanner.setVisible(false);
        errBanner.setManaged(false);

        // Success banner
        Label okBanner = new Label();
        okBanner.setStyle(
            "-fx-background-color:#f0fdf4; -fx-border-color:#bbf7d0;"
            + "-fx-border-radius:8; -fx-background-radius:8;"
            + "-fx-text-fill:#16a34a; -fx-font-size:12px; -fx-padding:8 12;");
        okBanner.setWrapText(true);
        okBanner.setMaxWidth(360);
        okBanner.setVisible(false);
        okBanner.setManaged(false);

        Label newLbl  = formLbl("New Password *");
        Label confLbl = formLbl("Confirm Password *");

        PasswordField newPw  = new PasswordField();
        PasswordField confPw = new PasswordField();
        newPw.setPromptText("Enter new password");
        confPw.setPromptText("Re-enter new password");
        newPw.setStyle(fieldStyle);
        confPw.setStyle(fieldStyle);
        newPw.setMaxWidth(360);
        confPw.setMaxWidth(360);

        // Buttons
        Button saveBtn   = new Button("Save Password");
        Button cancelBtn = new Button("Cancel");

        saveBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#2563eb,#1a4fa0);"
            + "-fx-text-fill:white; -fx-font-size:13px; -fx-font-weight:bold;"
            + "-fx-background-radius:8; -fx-padding:9 22; -fx-cursor:hand;");
        cancelBtn.setStyle(
            "-fx-background-color:#e2e8f0; -fx-text-fill:#475569;"
            + "-fx-font-size:13px; -fx-background-radius:8; -fx-padding:9 22; -fx-cursor:hand;");

        // Disable Save until both fields have text
        saveBtn.setDisable(true);
        newPw.textProperty().addListener((ob, o, n) ->
            saveBtn.setDisable(n.isBlank() || confPw.getText().isBlank()));
        confPw.textProperty().addListener((ob, o, n) ->
            saveBtn.setDisable(n.isBlank() || newPw.getText().isBlank()));

        HBox btnRow = new HBox(12, cancelBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(14, 0, 0, 0));

        card.getChildren().addAll(errBanner, okBanner, newLbl, newPw, confLbl, confPw, btnRow);
        root.getChildren().addAll(header, card);
        pwStage.setScene(new Scene(root, 420, 310));

        // -- Cancel button --
        cancelBtn.setOnAction(e -> pwStage.close());

        // -- Save button --
        saveBtn.setOnAction(e -> {
            String np = newPw.getText().trim();
            String cp = confPw.getText().trim();

            // Hide previous messages
            errBanner.setVisible(false); errBanner.setManaged(false);
            okBanner.setVisible(false);  okBanner.setManaged(false);

            if (np.length() < 4) {
                errBanner.setText("Password must be at least 4 characters.");
                errBanner.setVisible(true); errBanner.setManaged(true);
                return;
            }
            if (!np.equals(cp)) {
                errBanner.setText("Passwords do not match. Please try again.");
                errBanner.setVisible(true); errBanner.setManaged(true);
                newPw.clear(); confPw.clear(); newPw.requestFocus();
                return;
            }

            // Save to DB
            boolean saved = userDAO.updatePassword(verifiedAdmin.getId(), np);
            if (saved) {
                // Show success inside the same dialog, disable Save button, change Cancel to Close
                okBanner.setText("Password reset successfully!\n"
                    + "You can now log in with your new password.");
                okBanner.setVisible(true); okBanner.setManaged(true);
                saveBtn.setDisable(true);
                cancelBtn.setText("Close");
                newPw.setDisable(true);
                confPw.setDisable(true);
                // Auto-close after 3 s
                PauseTransition auto = new PauseTransition(Duration.seconds(3));
                auto.setOnFinished(ev -> pwStage.close());
                auto.play();
            } else {
                errBanner.setText("Failed to save password in database. Please try again.");
                errBanner.setVisible(true); errBanner.setManaged(true);
            }
        });

        pwStage.showAndWait();
    }

    // ---- Helpers --------------------------------------------------------
    private void stylePane(DialogPane dp) {
        dp.setStyle("-fx-background-color:white; -fx-font-family:'Segoe UI';");
    }

    private void infoAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(title);
        stylePane(a.getDialogPane());
        a.showAndWait();
    }

    private Label formLbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        return l;
    }

    private String cap(String s) {
        return (s == null || s.isEmpty()) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
