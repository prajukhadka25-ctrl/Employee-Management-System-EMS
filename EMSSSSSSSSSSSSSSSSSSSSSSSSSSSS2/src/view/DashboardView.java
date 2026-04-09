package view;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import model.Employee;
import model.User;

/**
 * VIEW LAYER — Employee list / CRUD table.
 * Has a Back button to return to Admin Dashboard.
 * INHERITANCE  — extends BaseView.
 * POLYMORPHISM — overrides getTitle().
 */
public class DashboardView extends BaseView {

    private TableView<Employee> table;
    private TextField           searchField;
    private Button              addButton, editButton, deleteButton,
                                refreshButton, logoutButton, backButton;
    private Label               statusLabel;

    public DashboardView(User user) {
        stage = new Stage();
        stage.setMinWidth(900); stage.setMinHeight(600);
        stage.setScene(buildScene(user));
    }

    // POLYMORPHISM — overrides abstract method from BaseView
    @Override public String getTitle() { return "EMS — Employees"; }

    @SuppressWarnings("unchecked")
    private Scene buildScene(User user) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f1f5f9;");

        // Top bar
        HBox top = new HBox(12);
        top.setPadding(new Insets(14, 20, 14, 20));
        top.setAlignment(Pos.CENTER_LEFT);
        top.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE,
                new Stop(0,Color.web("#1a4fa0")), new Stop(1,Color.web("#2563eb"))),
            CornerRadii.EMPTY, Insets.EMPTY)));

        backButton = navBtn("◀  Back");
        Label appTitle = new Label("👥  Employees");
        appTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:white;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label userInfo = new Label("👤 " + user.getUsername() + "  [ADMIN]");
        userInfo.setStyle("-fx-font-size:12px; -fx-text-fill:rgba(255,255,255,0.88);");
        logoutButton = navBtn("⬅  Logout");
        top.getChildren().addAll(backButton, appTitle, sp, userInfo, logoutButton);
        root.setTop(top);

        // Content
        VBox content = new VBox(16); content.setPadding(new Insets(20, 24, 20, 24));

        HBox toolbar = new HBox(10); toolbar.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("🔍  Search by name, department, email…");
        searchField.setPrefWidth(320);
        searchField.setStyle("-fx-background-color:white; -fx-border-color:#cbd5e1;" +
            "-fx-border-radius:8; -fx-background-radius:8;" +
            "-fx-font-size:13px; -fx-padding:8 12;");
        Region tbSp = new Region(); HBox.setHgrow(tbSp, Priority.ALWAYS);

        addButton     = actionBtn("＋  Add",    "#16a34a","#15803d");
        editButton    = actionBtn("✎  Edit",    "#2563eb","#1a4fa0");
        deleteButton  = actionBtn("✕  Delete",  "#dc2626","#b91c1c");
        refreshButton = actionBtn("↻  Refresh", "#64748b","#475569");

        toolbar.getChildren().addAll(searchField, tbSp,
            addButton, editButton, deleteButton, refreshButton);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius:10; -fx-border-radius:10; -fx-border-color:#e2e8f0;");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getColumns().addAll(
            col("ID",         "id",         60),
            col("First Name", "firstName",  110),
            col("Last Name",  "lastName",   110),
            col("Email",      "email",      190),
            col("Department", "department", 120),
            col("Position",   "position",   130),
            col("Salary",     "salary",     90),
            col("Hire Date",  "hireDate",   100));

        HBox statusBar = new HBox(); statusBar.setPadding(new Insets(6,4,0,4));
        statusLabel = new Label("Loading…");
        statusLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
        statusBar.getChildren().add(statusLabel);

        content.getChildren().addAll(toolbar, table, statusBar);
        root.setCenter(content);
        return new Scene(root, 1050, 660);
    }

    private <T> TableColumn<Employee, T> col(String t, String p, double w) {
        TableColumn<Employee,T> c = new TableColumn<>(t);
        c.setCellValueFactory(new PropertyValueFactory<>(p));
        c.setMinWidth(w); c.setStyle("-fx-font-size:12px;"); return c;
    }
    private Button actionBtn(String text, String bg, String hover) {
        Button btn = new Button(text); btn.setCursor(Cursor.HAND);
        String base = "-fx-background-color:"+bg+"; -fx-text-fill:white;" +
            "-fx-font-size:12px; -fx-font-weight:bold;" +
            "-fx-background-radius:8; -fx-padding:8 14;";
        String hov  = "-fx-background-color:"+hover+"; -fx-text-fill:white;" +
            "-fx-font-size:12px; -fx-font-weight:bold;" +
            "-fx-background-radius:8; -fx-padding:8 14;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hov));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }
    private Button navBtn(String text) {
        Button btn = new Button(text); btn.setCursor(Cursor.HAND);
        String base = "-fx-background-color:rgba(255,255,255,0.15); -fx-text-fill:white;" +
                      "-fx-font-size:12px; -fx-background-radius:7; -fx-padding:6 14;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base.replace("0.15","0.30")));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    // ── Public API ────────────────────────────────────────────
    // show(), hide(), close(), getStage(), setOnCloseRequest() — from BaseView
    public TableView<Employee> getTable()         { return table; }
    public TextField           getSearchField()   { return searchField; }
    public Button              getAddButton()     { return addButton; }
    public Button              getEditButton()    { return editButton; }
    public Button              getDeleteButton()  { return deleteButton; }
    public Button              getRefreshButton() { return refreshButton; }
    public Button              getLogoutButton()  { return logoutButton; }
    public Button              getBackButton()    { return backButton; }
    public void setStatusText(String text)        { statusLabel.setText(text); }
}
