package view;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.Cursor;

/**
 * VIEW LAYER — Login screen (split-panel design matching the screenshots).
 * INHERITANCE  — extends BaseView.
 * POLYMORPHISM — overrides getTitle().
 */
public class LoginView extends BaseView {

    private TextField        usernameField;
    private PasswordField    passwordField;
    private ComboBox<String> roleCombo;
    private Button           loginButton;
    private Hyperlink        forgotLink;
    private Label            errorLabel;

    public LoginView() {
        stage = new Stage();
        stage.setResizable(false);
        stage.setScene(buildScene());
    }

    // POLYMORPHISM — overrides abstract method from BaseView
    @Override
    public String getTitle() { return "Employee Management System — Login"; }

    // ── Scene ─────────────────────────────────────────────────
    private Scene buildScene() {
        HBox root = new HBox();

        // ── LEFT panel (blue branding) ─────────────────────────
        VBox left = new VBox(0);
        left.setPrefWidth(380);
        left.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#1E40AF")),
                new Stop(0.5, Color.web("#2563EB")),
                new Stop(1, Color.web("#3B82F6"))),
            CornerRadii.EMPTY, Insets.EMPTY)));
        left.setPadding(new Insets(50, 40, 40, 40));
        left.setSpacing(0);

        // Brand row
        HBox brandRow = new HBox(14);
        brandRow.setAlignment(Pos.CENTER_LEFT);
        Label briefcase = new Label("💼");
        briefcase.setStyle("-fx-font-size:32px;");
        Label emsLabel = new Label("EMS");
        emsLabel.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:white;");
        brandRow.getChildren().addAll(briefcase, emsLabel);

        VBox tagBox = new VBox(4);
        tagBox.setPadding(new Insets(60, 0, 0, 0));
        Label l1 = bold("Employee",   34);
        Label l2 = bold("Management", 34);
        Label l3 = bold("System",     34);
        Label desc = new Label("Manage your workforce efficiently.\nTrack attendance, leaves, and more.");
        desc.setStyle("-fx-font-size:14px; -fx-text-fill:rgba(255,255,255,0.82); -fx-padding:16 0 0 0;");
        desc.setWrapText(true);
        tagBox.getChildren().addAll(l1, l2, l3, desc);

        VBox features = new VBox(14);
        features.setPadding(new Insets(36, 0, 0, 0));
        features.getChildren().addAll(
            featureRow("✓",  "Manage employee records"),
            featureRow("📋", "Approve & track leave requests"),
            featureRow("📊", "Real-time attendance reports"),
            featureRow("🔒", "Role-based secure access")
        );

        Region lSpacer = new Region(); VBox.setVgrow(lSpacer, Priority.ALWAYS);
        left.getChildren().addAll(brandRow, tagBox, features, lSpacer);

        // ── RIGHT panel (white form) ───────────────────────────
        VBox right = new VBox(0);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 80, 40, 80));
        right.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        HBox.setHgrow(right, Priority.ALWAYS);

        Label welcome = new Label("Welcome back");
        welcome.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:#1E293B;");
        Label subtitle = new Label("Sign in to your account");
        subtitle.setStyle("-fx-font-size:14px; -fx-text-fill:#64748B; -fx-padding:6 0 28 0;");

        // Role combo
        Label roleLabel = formLabel("Login As");
        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Admin", "Employee");
        roleCombo.setPromptText("Select your role");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle("-fx-font-size:13px; -fx-pref-height:42px;" +
            "-fx-background-color:white; -fx-border-color:#CBD5E1;" +
            "-fx-border-radius:8; -fx-background-radius:8;");

        // Username
        Label unLabel = formLabel("Username");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        styleInput(usernameField);

        // Password
        Label pwLabel = formLabel("Password");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        styleInput(passwordField);

        // Forgot link
        forgotLink = new Hyperlink("Forgot password?");
        forgotLink.setStyle("-fx-text-fill:#2563EB; -fx-font-size:12px;" +
                            "-fx-border-color:transparent; -fx-cursor:hand;");
        HBox forgotRow = new HBox(); forgotRow.setAlignment(Pos.CENTER_RIGHT);
        forgotRow.getChildren().add(forgotLink);
        forgotRow.setPadding(new Insets(2, 0, 16, 0));

        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-background-color:#FEF2F2; -fx-border-color:#FECACA;" +
            "-fx-border-radius:8; -fx-background-radius:8;" +
            "-fx-text-fill:#DC2626; -fx-font-size:12px; -fx-padding:8 12;");
        errorLabel.setWrapText(true); errorLabel.setMaxWidth(320);
        errorLabel.setVisible(false); errorLabel.setManaged(false);

        // Login button
        loginButton = new Button("Sign In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setCursor(Cursor.HAND);
        loginButton.setStyle(
            "-fx-background-color:#1D4ED8; -fx-text-fill:white;" +
            "-fx-font-size:15px; -fx-font-weight:bold;" +
            "-fx-background-radius:10; -fx-pref-height:46px;");
        loginButton.setOnMouseEntered(e ->
            loginButton.setStyle(loginButton.getStyle().replace("#1D4ED8","#1E40AF")));
        loginButton.setOnMouseExited(e ->
            loginButton.setStyle(loginButton.getStyle().replace("#1E40AF","#1D4ED8")));

        Region rSpacer = new Region(); VBox.setVgrow(rSpacer, Priority.ALWAYS);
        Label copy = new Label("© 2025 EMS — All rights reserved");
        copy.setStyle("-fx-font-size:11px; -fx-text-fill:#94A3B8;");

        right.getChildren().addAll(
            welcome, subtitle,
            vGroup(roleLabel, roleCombo, 6),
            vGroup(unLabel,   usernameField, 6),
            vGroup(pwLabel,   passwordField, 6),
            forgotRow, errorLabel, loginButton,
            rSpacer, copy
        );

        root.getChildren().addAll(left, right);
        return new Scene(root, 1000, 650);
    }

    // ── Helpers ───────────────────────────────────────────────
    private Label bold(String text, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px; -fx-font-weight:bold; -fx-text-fill:white;");
        return l;
    }

    private HBox featureRow(String icon, String text) {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:13px; -fx-text-fill:white;");
        Label lbl = new Label(text); lbl.setStyle("-fx-font-size:13px; -fx-text-fill:rgba(255,255,255,0.9);");
        row.getChildren().addAll(ico, lbl);
        return row;
    }

    private Label formLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        return l;
    }

    private void styleInput(TextInputControl input) {
        String base = "-fx-font-size:13px; -fx-background-color:white;" +
                      "-fx-border-color:#CBD5E1; -fx-border-radius:8;" +
                      "-fx-background-radius:8; -fx-padding:10 12; -fx-pref-height:42px;";
        input.setStyle(base);
        input.setMaxWidth(Double.MAX_VALUE);
        input.focusedProperty().addListener((ob, o, focused) ->
            input.setStyle(focused
                ? base.replace("#CBD5E1","#2563EB")
                : base));
    }

    private VBox vGroup(Label label, Control control, int spacing) {
        VBox box = new VBox(spacing, label, control);
        box.setPadding(new Insets(0, 0, 14, 0));
        return box;
    }

    // ── Public API ────────────────────────────────────────────
    // show(), hide(), close(), getStage(), setOnCloseRequest()
    // are all INHERITED from BaseView

    public TextField      getUsernameField() { return usernameField; }
    public PasswordField  getPasswordField() { return passwordField; }
    public Button         getLoginButton()   { return loginButton; }
    public Hyperlink      getForgotLink()    { return forgotLink; }
    public String         getSelectedRole()  { return roleCombo.getValue(); }

    public void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    public void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    public void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Signing in…" : "Sign In");
    }
}
