package view;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import model.Attendance;
import model.LeaveRequest;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * VIEW LAYER — Admin Dashboard.
 * INHERITANCE  — extends BaseView.
 * POLYMORPHISM — overrides getTitle().
 *
 * Pure Java (no FXML). All UI is built programmatically.
 * The controller wires all callbacks via the public API at the bottom.
 */
// Inheritance - inherits properties from parent class i.e. Base View
public class AdminDashboardView extends BaseView {

    // ── Root layout ───────────────────────────────────────────
    private BorderPane rootPane;

    // ── Sidebar nav buttons ───────────────────────────────────
    private Button btnDashboard, btnEmployees, btnAddEmployee,
                   btnEditEmployee, btnDeleteEmployee, btnLeaveRequests, btnAttendance, btnLogout;

    // ── Stat card labels (updated by controller) ──────────────
    private Label lblTotalEmployees, lblPendingLeaves, lblDepartments;
    private Label lblAvgSalary, lblApprovedLeaves, lblRejectedLeaves;

    // ── Leave table & pie chart ───────────────────────────────
    private Canvas                  pieCanvas;
    private TableView<LeaveRequest> leaveTable;

    // ── Approve / Reject callbacks ────────────────────────────
    private Consumer<LeaveRequest> onApprove;
    private Consumer<LeaveRequest> onReject;

    public AdminDashboardView() {
        stage = new Stage();
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(buildScene());
    }

    // POLYMORPHISM — overrides abstract method from BaseView
    @Override public String getTitle() { return "EMS — Admin Dashboard"; }

    // ═══════════════════════════════════════════════════════════
    // SCENE
    // ═══════════════════════════════════════════════════════════
    private Scene buildScene() {
        rootPane = new BorderPane();
        rootPane.setLeft(buildSidebar());
        rootPane.setCenter(buildDashboardContent());
        return new Scene(rootPane, 1200, 750);
    }

    // ── SIDEBAR ────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color:#ffffff;" +
                         "-fx-border-color:#e2e8f0; -fx-border-width:0 1 0 0;");

        // Logo
        HBox logoRow = new HBox(10); logoRow.setAlignment(Pos.CENTER_LEFT);
        logoRow.setPadding(new Insets(22, 20, 22, 20));
        Label ico = new Label("💼"); ico.setStyle("-fx-font-size:22px;");
        VBox logoText = new VBox(2);
        Label logoMain = new Label("EMS System");
        logoMain.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");
        Label logoSub = new Label("Admin Panel");
        logoSub.setStyle("-fx-font-size:10px; -fx-text-fill:#94a3b8;");
        logoText.getChildren().addAll(logoMain, logoSub);
        logoRow.getChildren().addAll(ico, logoText);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#e2e8f0;");

        VBox nav = new VBox(2);
        nav.setPadding(new Insets(12, 8, 12, 8));
        btnDashboard      = navItem("⊞  Dashboard",         true);
        btnEmployees      = navItem("👥  Employees",         false);
        btnAddEmployee    = navItem("➕  Add Employee",      false);
        btnEditEmployee   = navItem("✎   Edit Employee",     false);
        btnDeleteEmployee = navItem("✕   Delete Employee",   false);
        btnLeaveRequests  = navItem("📋  Leave Requests",    false);
        btnAttendance     = navItem("📅  Attendance",        false);
        nav.getChildren().addAll(btnDashboard, btnEmployees, btnAddEmployee,
                                 btnEditEmployee, btnDeleteEmployee, btnLeaveRequests, btnAttendance);

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        btnLogout = new Button("⬅  Logout");
        btnLogout.setMaxWidth(Double.MAX_VALUE); btnLogout.setCursor(Cursor.HAND);
        String logBase = "-fx-background-color:transparent; -fx-text-fill:#64748b;" +
                         "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                         "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        btnLogout.setStyle(logBase);
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
            logBase.replace("transparent","#fef2f2").replace("#64748b","#dc2626")));
        btnLogout.setOnMouseExited(e  -> btnLogout.setStyle(logBase));

        VBox bottom = new VBox(4);
        bottom.setPadding(new Insets(0, 8, 16, 8));
        Separator sep2 = new Separator(); sep2.setStyle("-fx-background-color:#e2e8f0;");
        bottom.getChildren().addAll(sep2, btnLogout);

        sidebar.getChildren().addAll(logoRow, sep, nav, spacer, bottom);
        return sidebar;
    }

    // ── DASHBOARD CONTENT (default center) ────────────────────
    public ScrollPane buildDashboardContent() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        // Header
        VBox titleBlock = new VBox(3);
        Label title = new Label("Admin Dashboard");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");
        Label sub = new Label("Manage your organization efficiently");
        sub.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        titleBlock.getChildren().addAll(title, sub);

        // Stat cards row
        HBox statsRow = new HBox(16);
        lblTotalEmployees = new Label("0");
        lblPendingLeaves  = new Label("0");
        lblDepartments    = new Label("0");
        statsRow.getChildren().addAll(
            statCard("Total Employees",        lblTotalEmployees, "👥", "#eff6ff"),
            statCard("Pending Leave Requests", lblPendingLeaves,  "📄", "#fffbeb"),
            statCard("Departments",            lblDepartments,    "🏢", "#f0fdf4")
        );
        statsRow.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        // Middle row: pie chart + quick stats
        HBox midRow = new HBox(16);

        VBox pieCard = card();
        pieCard.setPrefWidth(460);
        Label pieTitle = new Label("Department Distribution");
        pieTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");
        pieCanvas = new Canvas(420, 240);
        pieCard.getChildren().addAll(pieTitle, pieCanvas);

        VBox quickCard = card();
        HBox.setHgrow(quickCard, Priority.ALWAYS);
        Label qTitle = new Label("Quick Statistics");
        qTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        lblAvgSalary      = new Label("$0");
        lblApprovedLeaves = new Label("0");
        lblRejectedLeaves = new Label("0");
        quickCard.getChildren().addAll(qTitle,
            quickStatRow("Average Salary",  lblAvgSalary,      "👥", "#eff6ff"),
            quickStatRow("Approved Leaves", lblApprovedLeaves, "📄", "#f0fdf4"),
            quickStatRow("Rejected Leaves", lblRejectedLeaves, "📄", "#fef2f2")
        );
        midRow.getChildren().addAll(pieCard, quickCard);

        // Leave requests table
        VBox leaveCard = card();
        HBox leaveTitleRow = new HBox(10); leaveTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label leaveTitle = new Label("Leave Requests");
        leaveTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        Label hint = new Label("Click ✔ Approve or ✖ Reject on any Pending row");
        hint.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8; -fx-font-style:italic;");
        leaveTitleRow.getChildren().addAll(leaveTitle, hint);

        leaveTable = buildLeaveTable();
        VBox.setVgrow(leaveTable, Priority.ALWAYS);
        leaveCard.getChildren().addAll(leaveTitleRow, leaveTable);

        main.getChildren().addAll(titleBlock, statsRow, midRow, leaveCard);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");
        return scroll;
    }

    // ── LEAVE TABLE ────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private TableView<LeaveRequest> buildLeaveTable() {
        TableView<LeaveRequest> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPrefHeight(300);
        tv.setStyle("-fx-background-radius:8; -fx-border-color:transparent;");
        tv.setPlaceholder(new Label("No leave requests found."));

        TableColumn<LeaveRequest, String> empCol = strCol("Employee", 130,
            lr -> lr.getEmployeeName());
        TableColumn<LeaveRequest, String> typeCol = strCol("Leave Type", 110,
            lr -> lr.getLeaveType());
        TableColumn<LeaveRequest, String> dateCol = strCol("Date Range", 180,
            lr -> lr.getDateRange());
        TableColumn<LeaveRequest, String> reasonCol = strCol("Reason", 200,
            lr -> lr.getReason());

        // Status badge column
        TableColumn<LeaveRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setMinWidth(100);
        statusCol.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(statusStyle(item) +
                    "-fx-background-radius:12; -fx-font-weight:bold;" +
                    "-fx-font-size:11px; -fx-alignment:CENTER; -fx-padding:3 10;");
            }
        });

        // Actions column
        TableColumn<LeaveRequest, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMinWidth(175); actionCol.setSortable(false);
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button appBtn = approveBtn();
            final Button rejBtn = rejectBtn();
            final HBox   box    = new HBox(6, appBtn, rejBtn);
            { box.setAlignment(Pos.CENTER); }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                LeaveRequest lr = getTableView().getItems().get(getIndex());
                boolean pending = "Pending".equals(lr.getStatus());
                appBtn.setDisable(!pending); appBtn.setOpacity(pending ? 1.0 : 0.35);
                rejBtn.setDisable(!pending); rejBtn.setOpacity(pending ? 1.0 : 0.35);
                appBtn.setOnAction(e -> { if (onApprove != null) onApprove.accept(lr); });
                rejBtn.setOnAction(e -> { if (onReject  != null) onReject.accept(lr); });
                setGraphic(box);
            }
        });

        tv.getColumns().addAll(empCol, typeCol, dateCol, reasonCol, statusCol, actionCol);
        return tv;
    }

    // ── Attendance overview table (swapped in as center) ──────
    public ScrollPane buildAttendanceContent(List<Attendance> list) {
        VBox main = new VBox(20);
        main.setPadding(new Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        Label title = new Label("Attendance Overview");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");
        Label sub = new Label("All employee attendance records");
        sub.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");

        VBox tableCard = card();
        TableView<Attendance> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPrefHeight(560);

        TableColumn<Attendance, String> cEmp  = strCol("Employee",  150, a -> a.getEmployeeName());
        TableColumn<Attendance, String> cDate = strCol("Date",      110, a -> a.getWorkDate().toString());
        TableColumn<Attendance, String> cIn   = strCol("Check In",  100, a ->
            a.getCheckIn()  != null ? a.getCheckIn().toString()  : "--");
        TableColumn<Attendance, String> cOut  = strCol("Check Out", 100, a ->
            a.getCheckOut() != null ? a.getCheckOut().toString() : "--");
        TableColumn<Attendance, String> cHrs  = strCol("Hours",      80, a -> a.getHoursWorked());

        tv.getColumns().addAll(cEmp, cDate, cIn, cOut, cHrs);
        tv.getItems().setAll(list);
        tableCard.getChildren().add(tv);
        main.getChildren().addAll(new VBox(3, title, sub), tableCard);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");
        return scroll;
    }

    // ── Helpers ───────────────────────────────────────────────
    private Button approveBtn() {
        Button btn = new Button("✔  Approve");
        String base = "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a;" +
            "-fx-font-size:11px; -fx-font-weight:bold;" +
            "-fx-background-radius:6; -fx-padding:4 10; -fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base.replace("#dcfce7","#bbf7d0").replace("#16a34a","#15803d")));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private Button rejectBtn() {
        Button btn = new Button("✖  Reject");
        String base = "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626;" +
            "-fx-font-size:11px; -fx-font-weight:bold;" +
            "-fx-background-radius:6; -fx-padding:4 10; -fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base.replace("#fee2e2","#fecaca").replace("#dc2626","#b91c1c")));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private String statusStyle(String status) {
        return switch (status) {
            case "Approved" -> "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a;";
            case "Rejected" -> "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626;";
            default         -> "-fx-background-color:#fef9c3; -fx-text-fill:#ca8a04;";
        };
    }

    private VBox statCard(String title, Label valueLabel, String icon, String bg) {
        VBox card = new VBox(6); card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white; -fx-background-radius:12;" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:12;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");
        HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT);
        VBox texts = new VBox(4);
        Label t = new Label(title); t.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");
        valueLabel.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        texts.getChildren().addAll(t, valueLabel);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label ico = new Label(icon); ico.setStyle(
            "-fx-background-color:" + bg + "; -fx-background-radius:10;" +
            "-fx-font-size:22px; -fx-padding:10 12;");
        row.getChildren().addAll(texts, sp, ico);
        card.getChildren().add(row);
        return card;
    }

    private HBox quickStatRow(String title, Label val, String icon, String bg) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color:" + bg + "; -fx-background-radius:10;");
        row.setMaxWidth(Double.MAX_VALUE);
        VBox texts = new VBox(2);
        Label t = new Label(title); t.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
        val.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        texts.getChildren().addAll(t, val);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:20px;");
        row.getChildren().addAll(texts, sp, ico);
        VBox.setMargin(row, new Insets(8, 0, 0, 0));
        return row;
    }

    public VBox card() {
        VBox v = new VBox(12); v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color:white; -fx-background-radius:12;" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:12;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");
        return v;
    }

    private Button navItem(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE); btn.setCursor(Cursor.HAND);
        String a = "-fx-background-color:#eff6ff; -fx-text-fill:#2563eb; -fx-font-weight:bold;" +
                   "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                   "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        String n = "-fx-background-color:transparent; -fx-text-fill:#475569;" +
                   "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                   "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        String h = "-fx-background-color:#f8fafc; -fx-text-fill:#1e293b;" +
                   "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                   "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        btn.setStyle(active ? a : n);
        btn.setOnMouseEntered(e -> { if (!btn.getStyle().contains("#eff6ff")) btn.setStyle(h); });
        btn.setOnMouseExited(e  -> { if (!btn.getStyle().contains("#eff6ff")) btn.setStyle(n); });
        return btn;
    }

    <T> TableColumn<T, String> strCol(String title, double minW,
            java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                extractor.apply(d.getValue())));
        col.setMinWidth(minW);
        return col;
    }

    public void drawPieChart(Map<String, Integer> data) {
        GraphicsContext gc = pieCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, pieCanvas.getWidth(), pieCanvas.getHeight());
        if (data == null || data.isEmpty()) return;
        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        Color[] colors = {Color.web("#3b82f6"),Color.web("#22c55e"),
                          Color.web("#f59e0b"),Color.web("#ef4444"),
                          Color.web("#a855f7"),Color.web("#06b6d4")};
        double cx = 120, cy = 115, r = 95, start = -90;
        int ci = 0;
        for (Map.Entry<String,Integer> e : data.entrySet()) {
            double sweep = (double)e.getValue() / total * 360;
            gc.setFill(colors[ci % colors.length]);
            gc.fillArc(cx-r, cy-r, r*2, r*2, start, sweep, javafx.scene.shape.ArcType.ROUND);
            start += sweep; ci++;
        }
        double lx = 255, ly = 20; ci = 0;
        gc.setFont(Font.font("Segoe UI", 12));
        for (Map.Entry<String,Integer> e : data.entrySet()) {
            double pct = (double)e.getValue()/total;
            gc.setFill(colors[ci % colors.length]);
            gc.fillRoundRect(lx, ly + ci*28, 12, 12, 4, 4);
            gc.setFill(Color.web("#1e293b"));
            gc.fillText(e.getKey() + ": " + Math.round(pct*100) + "%", lx+18, ly+ci*28+11);
            ci++;
        }
    }

    // ── Swap center content ────────────────────────────────────
    public void showCenter(Node content) { rootPane.setCenter(content); }

    // ── Nav active highlight ───────────────────────────────────
    public void setNavActive(Button active) {
        String a = "-fx-background-color:#eff6ff; -fx-text-fill:#2563eb; -fx-font-weight:bold;" +
                   "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                   "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        String n = "-fx-background-color:transparent; -fx-text-fill:#475569;" +
                   "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                   "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        for (Button b : new Button[]{btnDashboard,btnEmployees,btnAddEmployee,
                                     btnEditEmployee,btnDeleteEmployee,btnLeaveRequests,btnAttendance})
            b.setStyle(b == active ? a : n);
    }

    // ── Public API ─────────────────────────────────────────────
    // show(), hide(), close(), getStage(), setOnCloseRequest() — from BaseView

    public Button              getBtnDashboard()      { return btnDashboard; }
    public Button              getBtnEmployees()      { return btnEmployees; }
    public Button              getBtnAddEmployee()    { return btnAddEmployee; }
    public Button              getBtnEditEmployee()   { return btnEditEmployee; }
    public Button              getBtnDeleteEmployee() { return btnDeleteEmployee; }
    public Button              getBtnLeaveRequests()  { return btnLeaveRequests; }
    public Button              getBtnAttendance()     { return btnAttendance; }
    public Button              getBtnLogout()         { return btnLogout; }
    public TableView<LeaveRequest> getLeaveTable()    { return leaveTable; }

    public void setTotalEmployees(int n)  { lblTotalEmployees.setText(String.valueOf(n)); }
    public void setPendingLeaves(int n)   { lblPendingLeaves.setText(String.valueOf(n)); }
    public void setDepartments(int n)     { lblDepartments.setText(String.valueOf(n)); }
    public void setAvgSalary(double d)    { lblAvgSalary.setText("$" + String.format("%.0f",d)); }
    public void setApprovedLeaves(int n)  { lblApprovedLeaves.setText(String.valueOf(n)); }
    public void setRejectedLeaves(int n)  { lblRejectedLeaves.setText(String.valueOf(n)); }

    public void setOnApprove(Consumer<LeaveRequest> h) { onApprove = h; }
    public void setOnReject(Consumer<LeaveRequest>  h) { onReject  = h; }
}
