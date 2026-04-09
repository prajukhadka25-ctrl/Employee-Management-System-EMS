package view;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import model.Attendance;
import model.Employee;
import model.LeaveRequest;
import model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * VIEW LAYER — Employee Dashboard (no FXML).
 * INHERITANCE  — extends BaseView.
 * POLYMORPHISM — overrides getTitle().
 *
 * Builds every page as a method that returns a Node;
 * the controller calls showCenter(node) to swap the view.
 */
public class EmployeeDashboardView extends BaseView {

    // ── Sidebar buttons ───────────────────────────────────────
    private Button btnDashboard, btnProfile, btnAttendance, btnApplyLeave, btnLogout;

    // ── Stat card labels ──────────────────────────────────────
    private Label lblTotalAttendance, lblApprovedLeaves, lblPendingLeaves;

    // ── Charts ────────────────────────────────────────────────
    private Canvas barCanvas;

    // ── Recent leave list container ───────────────────────────
    private VBox leaveListBox;

    // ── Quick action buttons ──────────────────────────────────
    private Button btnMarkAttendance, btnApplyLeaveAction, btnViewLeaveStatus;

    // ── Root layout ───────────────────────────────────────────
    private BorderPane rootPane;

    // ── Profile page labels ───────────────────────────────────
    private Label profileFullName, profileRole, profileDept,
                  profileEmail, profilePosition, profileSalary,
                  profileHireDate, profileEmployeeId, profileDeptDetail;
    private Button btnEditProfile;

    // ── Sidebar name labels ───────────────────────────────────
    private Label sidebarName, sidebarRole;

    public EmployeeDashboardView(User user, String fullName) {
        stage = new Stage();
        stage.setMinWidth(960); stage.setMinHeight(660);
        stage.setScene(buildScene(user, fullName));
    }

    // POLYMORPHISM
    @Override public String getTitle() { return "EMS — Employee Dashboard"; }

    // ═══════════════════════════════════════════════════════════
    private Scene buildScene(User user, String fullName) {
        rootPane = new BorderPane();
        rootPane.setLeft(buildSidebar(user, fullName));
        rootPane.setCenter(buildDashboardContent(fullName));
        return new Scene(rootPane, 1100, 720);
    }

    // ── SIDEBAR ────────────────────────────────────────────────
    private VBox buildSidebar(User user, String fullName) {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color:#ffffff;" +
                         "-fx-border-color:#e2e8f0; -fx-border-width:0 1 0 0;");

        HBox logoRow = new HBox(10); logoRow.setAlignment(Pos.CENTER_LEFT);
        logoRow.setPadding(new Insets(22, 20, 22, 20));
        Label ico = new Label("💼"); ico.setStyle("-fx-font-size:22px;");
        VBox lt = new VBox(2);
        Label lm = new Label("EMS System");
        lm.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");
        Label ls = new Label("Employee Portal");
        ls.setStyle("-fx-font-size:10px; -fx-text-fill:#94a3b8;");
        lt.getChildren().addAll(lm, ls);
        logoRow.getChildren().addAll(ico, lt);

        Separator sep1 = new Separator(); sep1.setStyle("-fx-background-color:#e2e8f0;");

        // Mini user card
        HBox userCard = new HBox(10); userCard.setPadding(new Insets(14,16,14,16));
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setStyle("-fx-background-color:#f8fafc;");
        StackPane avatar = makeAvatar(fullName, 38);
        VBox userTexts = new VBox(2);
        sidebarName = new Label(fullName);
        sidebarName.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        sidebarName.setMaxWidth(140);
        sidebarRole = new Label("Employee");
        sidebarRole.setStyle("-fx-font-size:11px; -fx-text-fill:#2563eb;");
        userTexts.getChildren().addAll(sidebarName, sidebarRole);
        userCard.getChildren().addAll(avatar, userTexts);

        Separator sep2 = new Separator(); sep2.setStyle("-fx-background-color:#e2e8f0;");

        VBox nav = new VBox(2); nav.setPadding(new Insets(10, 8, 10, 8));
        btnDashboard  = navItem("⊞  Dashboard",   true);
        btnProfile    = navItem("👤  My Profile",  false);
        btnAttendance = navItem("📅  Attendance",  false);
        btnApplyLeave = navItem("📋  Apply Leave", false);
        nav.getChildren().addAll(btnDashboard, btnProfile, btnAttendance, btnApplyLeave);

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

        VBox bottom = new VBox(4); bottom.setPadding(new Insets(0, 8, 16, 8));
        Separator sep3 = new Separator(); sep3.setStyle("-fx-background-color:#e2e8f0;");
        bottom.getChildren().addAll(sep3, btnLogout);

        sidebar.getChildren().addAll(logoRow, sep1, userCard, sep2, nav, spacer, bottom);
        return sidebar;
    }

    // ── DASHBOARD HOME ────────────────────────────────────────
    public ScrollPane buildDashboardContent(String fullName) {
        VBox main = new VBox(20);
        main.setPadding(new Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        VBox hdr = new VBox(3);
        Label title = new Label("Welcome, " + fullName);
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        Label sub = new Label("Here's your performance overview");
        sub.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        hdr.getChildren().addAll(title, sub);

        HBox stats = new HBox(16);
        lblTotalAttendance = new Label("0");
        lblApprovedLeaves  = new Label("0");
        lblPendingLeaves   = new Label("0");
        stats.getChildren().addAll(
            statCard("Total Attendance", lblTotalAttendance, "📅", "#eff6ff"),
            statCard("Approved Leaves",  lblApprovedLeaves,  "📄", "#f0fdf4"),
            statCard("Pending Leaves",   lblPendingLeaves,   "⏰", "#fffbeb")
        );
        stats.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        HBox midRow = new HBox(16);

        // Bar chart card
        VBox barCard = card(); HBox.setHgrow(barCard, Priority.ALWAYS);
        Label barTitle = new Label("Monthly Attendance");
        barTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        barCanvas = new Canvas(420, 240);
        barCard.getChildren().addAll(barTitle, barCanvas);

        // Recent leaves
        VBox leaveCard = card(); leaveCard.setPrefWidth(300);
        Label leaveTitle = new Label("Recent Leave Requests");
        leaveTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        leaveListBox = new VBox(8);
        leaveCard.getChildren().addAll(leaveTitle, leaveListBox);

        midRow.getChildren().addAll(barCard, leaveCard);

        // Quick actions
        VBox qaCard = card();
        Label qaTitle = new Label("Quick Actions");
        qaTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        btnMarkAttendance   = qaBtn("↗  Mark Attendance");
        btnApplyLeaveAction = qaBtn("📄  Apply for Leave");
        btnViewLeaveStatus  = qaBtn("⏰  View Leave Status");
        HBox qaRow = new HBox(12,btnMarkAttendance,btnApplyLeaveAction,btnViewLeaveStatus);
        qaCard.getChildren().addAll(qaTitle, qaRow);

        main.getChildren().addAll(hdr, stats, midRow, qaCard);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");
        return scroll;
    }

    // ── ATTENDANCE PAGE ────────────────────────────────────────
    /**
     * Builds the Attendance page with today's card + full history table.
     * @param todayAtt  today's Attendance record (may be null)
     * @param history   full attendance history list
     * @param onCheckIn  callback for Check-In button
     * @param onCheckOut callback for Check-Out button
     */
    public ScrollPane buildAttendancePage(Attendance todayAtt,
                                          List<Attendance> history,
                                          Runnable onCheckIn,
                                          Runnable onCheckOut) {
        VBox main = new VBox(20);
        main.setPadding(new Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        VBox hdr = new VBox(3);
        Label t = new Label("My Attendance");
        t.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        Label s = new Label("Track your daily check-in / check-out records");
        s.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        hdr.getChildren().addAll(t, s);

        // ── Today card ─────────────────────────────────────────
        VBox todayCard = card();
        Label tTitle = new Label("Today — " + LocalDate.now());
        tTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");
        todayCard.getChildren().add(tTitle);

        if (todayAtt == null) {
            // Not yet checked in
            Label noAtt = new Label("You haven't checked in yet today.");
            noAtt.setStyle("-fx-text-fill:#64748b; -fx-font-size:13px;");
            Button ciBtn = actionBtn("✓  Check In", "#16a34a", "#15803d");
            ciBtn.setPrefHeight(42); ciBtn.setOnAction(e -> onCheckIn.run());
            todayCard.getChildren().addAll(noAtt, ciBtn);
        } else {
            HBox infoRow = new HBox(40); infoRow.setAlignment(Pos.CENTER_LEFT);
            infoRow.getChildren().addAll(
                timeBox("Check In",
                    todayAtt.getCheckIn() != null ? todayAtt.getCheckIn().toString() : "--",
                    "#16a34a"),
                timeBox("Check Out",
                    todayAtt.getCheckOut() != null ? todayAtt.getCheckOut().toString() : "Pending…",
                    todayAtt.getCheckOut() != null ? "#dc2626" : "#d97706"),
                timeBox("Hours Worked", todayAtt.getHoursWorked(), "#2563eb")
            );
            todayCard.getChildren().add(infoRow);

            if (todayAtt.getCheckOut() == null) {
                Button coBtn = actionBtn("✖  Check Out", "#dc2626", "#b91c1c");
                coBtn.setPrefHeight(42); coBtn.setOnAction(e -> onCheckOut.run());
                todayCard.getChildren().add(coBtn);
            } else {
                Label done = new Label("✓  Attendance complete for today.");
                done.setStyle("-fx-text-fill:#16a34a; -fx-font-size:13px; -fx-font-weight:bold;");
                todayCard.getChildren().add(done);
            }
        }

        // ── History table ──────────────────────────────────────
        VBox histCard = card();
        Label histTitle = new Label("Attendance History");
        histTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");

        TableView<Attendance> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPrefHeight(380);

        tv.getColumns().addAll(
            attCol("Date",       130, a -> a.getWorkDate().toString()),
            attCol("Check In",   110, a -> a.getCheckIn()  != null ? a.getCheckIn().toString()  : "--"),
            attCol("Check Out",  110, a -> a.getCheckOut() != null ? a.getCheckOut().toString() : "--"),
            attCol("Hours",       80, a -> a.getHoursWorked())
        );
        tv.getItems().setAll(history);
        histCard.getChildren().addAll(histTitle, tv);

        main.getChildren().addAll(hdr, todayCard, histCard);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");
        return scroll;
    }

    // ── APPLY LEAVE PAGE ───────────────────────────────────────
    /**
     * Builds the Apply Leave page with the request form and history table.
     * Returns a Node array: [0]=scrollPane, [1]=leaveTypeCombo,
     * [2]=startPicker, [3]=endPicker, [4]=reasonArea, [5]=submitBtn, [6]=histTable
     */
    public Object[] buildApplyLeavePage(List<LeaveRequest> history) {
        VBox main = new VBox(20);
        main.setPadding(new Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        VBox hdr = new VBox(3);
        Label t  = new Label("Leave Management");
        t.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        Label s  = new Label("Apply for leave and view your request history");
        s.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        hdr.getChildren().addAll(t, s);

        // ── Application form ────────────────────────────────────
        VBox formCard = card(); formCard.setMaxWidth(680);
        Label formTitle = new Label("Apply for Leave");
        formTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Sick Leave","Vacation","Personal",
                                    "Maternity/Paternity","Unpaid Leave");
        typeCombo.setPromptText("Select leave type");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-font-size:13px; -fx-pref-height:38px;" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        DatePicker startPicker = new DatePicker(LocalDate.now());
        startPicker.setMaxWidth(Double.MAX_VALUE);
        DatePicker endPicker   = new DatePicker(LocalDate.now());
        endPicker.setMaxWidth(Double.MAX_VALUE);

        GridPane dateGrid = new GridPane(); dateGrid.setHgap(16); dateGrid.setVgap(12);
        dateGrid.getColumnConstraints().addAll(
            col50(), col50());
        dateGrid.add(fLbl("Leave Type *"), 0, 0); dateGrid.add(typeCombo, 0, 1);
        dateGrid.add(fLbl("Start Date *"), 1, 0); dateGrid.add(startPicker, 1, 1);

        GridPane dateGrid2 = new GridPane(); dateGrid2.setHgap(16); dateGrid2.setVgap(12);
        dateGrid2.getColumnConstraints().addAll(col50(), col50());
        dateGrid2.add(fLbl("End Date *"), 0, 0); dateGrid2.add(endPicker, 0, 1);

        Label reasonLbl = fLbl("Reason *");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Describe the reason for your leave request…");
        reasonArea.setPrefRowCount(4); reasonArea.setWrapText(true);
        reasonArea.setStyle("-fx-font-size:13px; -fx-background-color:white;" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        Button submitBtn = new Button("Submit Leave Request");
        submitBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white;" +
            "-fx-font-size:13px; -fx-font-weight:bold;" +
            "-fx-background-radius:8; -fx-padding:10 24;");
        submitBtn.setCursor(Cursor.HAND);

        formCard.getChildren().addAll(formTitle, dateGrid, dateGrid2, reasonLbl, reasonArea, submitBtn);

        // ── History table ──────────────────────────────────────
        VBox histCard = card();
        Label histTitle = new Label("My Leave Requests");
        histTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");

        TableView<LeaveRequest> histTable = new TableView<>();
        histTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        histTable.setPrefHeight(320);

        histTable.getColumns().addAll(
            lrCol("Leave Type",    120, lr -> lr.getLeaveType()),
            lrCol("Date Range",    175, lr -> lr.getDateRange()),
            lrCol("Days",           55, lr -> String.valueOf(lr.getDays())),
            lrCol("Reason",        190, lr -> lr.getReason()),
            statusBadgeCol(),
            lrCol("Admin Note",    150, lr -> lr.getAdminNote())
        );
        histTable.getItems().setAll(history);
        histCard.getChildren().addAll(histTitle, histTable);

        main.getChildren().addAll(hdr, formCard, histCard);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");

        return new Object[]{ scroll, typeCombo, startPicker, endPicker, reasonArea, submitBtn, histTable };
    }

    // ── PROFILE PAGE ──────────────────────────────────────────
    public ScrollPane buildProfilePage(String fullName) {
        VBox main = new VBox(24);
        main.setPadding(new Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        VBox hdr = new VBox(4);
        Label t = new Label("My Profile");
        t.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        Label s = new Label("Your personal & employment details");
        s.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        hdr.getChildren().addAll(t, s);

        // Hero card
        HBox hero = new HBox(24); hero.setPadding(new Insets(28));
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setStyle("-fx-background-color:linear-gradient(to right,#1a4fa0,#2563eb);" +
                      "-fx-background-radius:16;" +
                      "-fx-effect:dropshadow(gaussian,rgba(37,99,235,0.25),12,0,0,4);");
        StackPane bigAvatar = makeAvatar(fullName, 80);
        VBox heroText = new VBox(6); heroText.setAlignment(Pos.CENTER_LEFT);
        profileFullName = new Label(fullName);
        profileFullName.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:white;");
        profileRole = new Label("Employee");
        profileRole.setStyle("-fx-background-color:rgba(255,255,255,0.2); -fx-text-fill:white;" +
            "-fx-font-size:12px; -fx-background-radius:20; -fx-padding:3 12;");
        profileDept = new Label("—");
        profileDept.setStyle("-fx-font-size:13px; -fx-text-fill:rgba(255,255,255,0.8);");
        heroText.getChildren().addAll(profileFullName, profileRole, profileDept);
        Region hsp = new Region(); HBox.setHgrow(hsp, Priority.ALWAYS);
        VBox idBadge = new VBox(4); idBadge.setAlignment(Pos.CENTER);
        idBadge.setPadding(new Insets(12,20,12,20));
        idBadge.setStyle("-fx-background-color:rgba(255,255,255,0.15); -fx-background-radius:12;");
        Label idLbl = new Label("Employee ID"); idLbl.setStyle("-fx-font-size:10px; -fx-text-fill:rgba(255,255,255,0.7);");
        profileEmployeeId = new Label("#—");
        profileEmployeeId.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:white;");
        idBadge.getChildren().addAll(idLbl, profileEmployeeId);
        hero.getChildren().addAll(bigAvatar, heroText, hsp, idBadge);

        // Details card
        VBox detCard = card();
        Label detTitle = new Label("Personal & Employment Information");
        detTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        GridPane grid = new GridPane(); grid.setHgap(24); grid.setVgap(0);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setPercentWidth(50);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc1, cc2);
        profileEmail    = new Label("—");
        profilePosition = new Label("—");
        profileDeptDetail = new Label("—");
        profileSalary   = new Label("—");
        profileHireDate = new Label("—");
        grid.add(infoField("📧  Email",    profileEmail),    0, 0);
        grid.add(infoField("💼  Position", profilePosition), 1, 0);
        grid.add(infoField("🏢  Department",profileDeptDetail),0,1);
        grid.add(infoField("💰  Salary",   profileSalary),  1, 1);
        grid.add(infoField("📅  Hire Date",profileHireDate), 0, 2);

        btnEditProfile = new Button("✎  Edit Profile"); btnEditProfile.setCursor(Cursor.HAND);
        btnEditProfile.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white;" +
            "-fx-font-size:13px; -fx-font-weight:bold;" +
            "-fx-background-radius:8; -fx-padding:10 24;");
        HBox bRow = new HBox(); bRow.setAlignment(Pos.CENTER_RIGHT);
        bRow.setPadding(new Insets(12,0,0,0)); bRow.getChildren().add(btnEditProfile);
        detCard.getChildren().addAll(detTitle, new Separator(), grid, bRow);

        main.getChildren().addAll(hdr, hero, detCard);
        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");
        return scroll;
    }

    // ── EDIT PROFILE PAGE (inline, no modal) ──────────────────
    /**
     * Builds a fully editable profile page.
     * Returns Object[] so the controller can wire Save/Cancel/password buttons:
     *   [0] ScrollPane  – the page node to pass to showCenter()
     *   [1] TextField   – firstNameField
     *   [2] TextField   – lastNameField
     *   [3] TextField   – emailField
     *   [4] TextField   – phoneField (optional)
     *   [5] Button      – saveInfoBtn
     *   [6] Button      – cancelBtn
     *   [7] PasswordField – currentPwField
     *   [8] PasswordField – newPwField
     *   [9] PasswordField – confirmPwField
     *   [10] Button     – savePwBtn
     *   [11] Label      – infoStatusLabel  (green/red feedback for info save)
     *   [12] Label      – pwStatusLabel    (green/red feedback for password save)
     */
    public Object[] buildEditProfilePage(Employee emp) {
        VBox main = new VBox(24);
        main.setPadding(new Insets(28));
        main.setStyle("-fx-background-color:#f8fafc;");

        // ---- Header -------------------------------------------------
        VBox hdr = new VBox(4);
        Label t = new Label("Edit My Profile");
        t.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        Label s = new Label("Update your personal information and password");
        s.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        hdr.getChildren().addAll(t, s);

        String fs = "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0;"
            + "-fx-border-radius:8; -fx-background-radius:8;"
            + "-fx-font-size:13px; -fx-padding:9 12;";

        // ---- Personal Information card ------------------------------
        VBox infoCard = card();
        Label infoTitle = new Label("Personal Information");
        infoTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");

        // Status label (green/red) for info save
        Label infoStatus = new Label();
        infoStatus.setWrapText(true);
        infoStatus.setMaxWidth(540);
        infoStatus.setVisible(false);
        infoStatus.setManaged(false);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(16); infoGrid.setVgap(14);
        ColumnConstraints ic1 = new ColumnConstraints(130);
        ColumnConstraints ic2 = new ColumnConstraints(); ic2.setHgrow(Priority.ALWAYS);
        ColumnConstraints ic3 = new ColumnConstraints(130);
        ColumnConstraints ic4 = new ColumnConstraints(); ic4.setHgrow(Priority.ALWAYS);
        infoGrid.getColumnConstraints().addAll(ic1, ic2, ic3, ic4);

        TextField firstNameF = editField(emp != null ? emp.getFirstName() : "", "First Name", fs);
        TextField lastNameF  = editField(emp != null ? emp.getLastName()  : "", "Last Name",  fs);
        TextField emailF     = editField(emp != null ? emp.getEmail()     : "", "Email address", fs);
        TextField phoneF     = editField("", "Phone number", fs);   // employee has no phone in base model

        infoGrid.add(editLbl("First Name *"), 0, 0); infoGrid.add(firstNameF, 1, 0);
        infoGrid.add(editLbl("Last Name *"),  2, 0); infoGrid.add(lastNameF,  3, 0);
        infoGrid.add(editLbl("Email *"),      0, 1); infoGrid.add(emailF,     1, 1);

        // Read-only fields (salary, department, position set by admin)
        Label roNote = new Label("Department, Position and Salary can only be changed by an administrator.");
        roNote.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8; -fx-font-style:italic;");

        Button saveInfoBtn = actionBtn("💾  Save Changes", "#2563eb", "#1a4fa0");
        Button cancelBtn   = new Button("✕  Cancel");
        cancelBtn.setCursor(Cursor.HAND);
        cancelBtn.setStyle("-fx-background-color:#e2e8f0; -fx-text-fill:#475569;"
            + "-fx-font-size:13px; -fx-background-radius:8; -fx-padding:9 22;");

        HBox infoButtons = new HBox(12, cancelBtn, saveInfoBtn);
        infoButtons.setAlignment(Pos.CENTER_RIGHT);
        infoButtons.setPadding(new Insets(10, 0, 0, 0));

        infoCard.getChildren().addAll(infoTitle, infoStatus, infoGrid, roNote, infoButtons);

        // ---- Change Password card -----------------------------------
        VBox pwCard = card();
        Label pwTitle = new Label("Change Password");
        pwTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2563eb;");

        Label pwStatus = new Label();
        pwStatus.setWrapText(true);
        pwStatus.setMaxWidth(540);
        pwStatus.setVisible(false);
        pwStatus.setManaged(false);

        GridPane pwGrid = new GridPane();
        pwGrid.setHgap(16); pwGrid.setVgap(14);
        ColumnConstraints pc1 = new ColumnConstraints(160);
        ColumnConstraints pc2 = new ColumnConstraints(); pc2.setHgrow(Priority.ALWAYS);
        pwGrid.getColumnConstraints().addAll(pc1, pc2);

        PasswordField curPwF  = new PasswordField(); curPwF.setPromptText("Enter your current password");
        PasswordField newPwF  = new PasswordField(); newPwF.setPromptText("Enter new password (min 4 chars)");
        PasswordField confPwF = new PasswordField(); confPwF.setPromptText("Re-enter new password");
        curPwF.setStyle(fs); curPwF.setMaxWidth(Double.MAX_VALUE);
        newPwF.setStyle(fs); newPwF.setMaxWidth(Double.MAX_VALUE);
        confPwF.setStyle(fs); confPwF.setMaxWidth(Double.MAX_VALUE);

        pwGrid.add(editLbl("Current Password *"), 0, 0); pwGrid.add(curPwF,  1, 0);
        pwGrid.add(editLbl("New Password *"),      0, 1); pwGrid.add(newPwF,  1, 1);
        pwGrid.add(editLbl("Confirm Password *"),  0, 2); pwGrid.add(confPwF, 1, 2);

        Label pwHint = new Label("Password must be at least 4 characters.");
        pwHint.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");

        Button savePwBtn = actionBtn("🔒  Update Password", "#16a34a", "#15803d");

        HBox pwButtons = new HBox(12, savePwBtn);
        pwButtons.setAlignment(Pos.CENTER_RIGHT);
        pwButtons.setPadding(new Insets(10, 0, 0, 0));

        pwCard.getChildren().addAll(pwTitle, pwStatus, pwGrid, pwHint, pwButtons);

        main.getChildren().addAll(hdr, infoCard, pwCard);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#f8fafc; -fx-background:#f8fafc;");

        return new Object[]{ scroll, firstNameF, lastNameF, emailF, phoneF,
                             saveInfoBtn, cancelBtn,
                             curPwF, newPwF, confPwF, savePwBtn,
                             infoStatus, pwStatus };
    }

    // ── Helpers for edit form ─────────────────────────────────
    private TextField editField(String value, String prompt, String style) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setStyle(style);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private Label editLbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        return l;
    }

    private Button actionBtn(String text, String bg, String hov) {
        Button btn = new Button(text);
        btn.setCursor(Cursor.HAND);
        String base = "-fx-background-color:" + bg + "; -fx-text-fill:white;"
            + "-fx-font-size:13px; -fx-font-weight:bold;"
            + "-fx-background-radius:8; -fx-padding:9 22;";
        btn.setStyle(base);
        String hovStyle = "-fx-background-color:" + hov + "; -fx-text-fill:white;"
            + "-fx-font-size:13px; -fx-font-weight:bold;"
            + "-fx-background-radius:8; -fx-padding:9 22;";
        btn.setOnMouseEntered(e -> btn.setStyle(hovStyle));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    /** Show status inside edit profile page (green = success, red = error). */
    public static void showStatus(Label lbl, String msg, boolean success) {
        lbl.setText(msg);
        lbl.setStyle(success
            ? "-fx-background-color:#f0fdf4; -fx-border-color:#bbf7d0;"
              + "-fx-border-radius:8; -fx-background-radius:8;"
              + "-fx-text-fill:#16a34a; -fx-font-size:12px; -fx-padding:8 12;"
            : "-fx-background-color:#fef2f2; -fx-border-color:#fecaca;"
              + "-fx-border-radius:8; -fx-background-radius:8;"
              + "-fx-text-fill:#dc2626; -fx-font-size:12px; -fx-padding:8 12;");
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    /** Populate the profile labels from an Employee model object. */
    public void populateProfile(Employee emp) {
        if (emp == null) return;
        profileFullName.setText(emp.getFullName());
        profileEmployeeId.setText("#" + emp.getId());
        profileEmail.setText(emp.getEmail() != null ? emp.getEmail() : "—");
        profileDept.setText(emp.getDepartment() != null ? emp.getDepartment() : "—");
        if (profileDeptDetail != null) profileDeptDetail.setText(profileDept.getText());
        profilePosition.setText(emp.getPosition() != null ? emp.getPosition() : "—");
        profileSalary.setText(emp.getSalary() != null
            ? "$" + String.format("%,.2f", emp.getSalary()) : "—");
        profileHireDate.setText(emp.getHireDate() != null ? emp.getHireDate().toString() : "—");
        sidebarName.setText(emp.getFullName());
        if (emp.getPosition() != null) sidebarRole.setText(emp.getPosition());
    }

    // ── Swap center ────────────────────────────────────────────
    public void showCenter(Node content) { rootPane.setCenter(content); }

    // ── Update leave list (dashboard home) ────────────────────
    public void setLeaveRequests(List<LeaveRequest> list) {
        if (leaveListBox == null) return;
        leaveListBox.getChildren().clear();
        for (LeaveRequest lr : list) {
            VBox item = new VBox(2); item.setPadding(new Insets(10,12,10,12));
            item.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:8;");
            HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT);
            Label type = new Label(lr.getLeaveType());
            type.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            String sc = switch (lr.getStatus()) {
                case "Approved" -> "#dcfce7; -fx-text-fill:#16a34a;";
                case "Rejected" -> "#fee2e2; -fx-text-fill:#dc2626;";
                default         -> "#fef9c3; -fx-text-fill:#ca8a04;";
            };
            Label status = new Label(lr.getStatus());
            status.setStyle("-fx-background-color:" + sc +
                " -fx-background-radius:10; -fx-font-size:10px;" +
                "-fx-font-weight:bold; -fx-padding:2 8;");
            row.getChildren().addAll(type, sp, status);
            Label dates = new Label(lr.getDateRange());
            dates.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
            item.getChildren().addAll(row, dates);
            leaveListBox.getChildren().add(item);
        }
        if (list.isEmpty()) {
            Label empty = new Label("No leave requests yet.");
            empty.setStyle("-fx-font-size:12px; -fx-text-fill:#94a3b8;");
            leaveListBox.getChildren().add(empty);
        }
    }

    // ── Nav highlight ─────────────────────────────────────────
    public void setNavActive(Button active) {
        String a = "-fx-background-color:#eff6ff; -fx-text-fill:#2563eb; -fx-font-weight:bold;" +
                   "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                   "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        String n = "-fx-background-color:transparent; -fx-text-fill:#475569;" +
                   "-fx-font-size:13px; -fx-alignment:CENTER_LEFT;" +
                   "-fx-padding:10 12 10 16; -fx-background-radius:8;";
        for (Button b : new Button[]{btnDashboard,btnProfile,btnAttendance,btnApplyLeave})
            b.setStyle(b == active ? a : n);
    }

    // ═══════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════
    public void drawBarChart(int[] monthly) {
        if (barCanvas == null) return;
        GraphicsContext gc = barCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, barCanvas.getWidth(), barCanvas.getHeight());
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun"};
        double w = barCanvas.getWidth(), h = barCanvas.getHeight();
        double padL=42, padB=36, padT=16, padR=16;
        double chartW=w-padL-padR, chartH=h-padB-padT;
        int max=24; for (int v:monthly) if (v>max) max=v;
        gc.setFont(Font.font("Segoe UI",11));
        for (int i=0;i<=4;i++) {
            double y = padT+chartH-(chartH*i/4.0);
            gc.setStroke(Color.web("#e2e8f0")); gc.setLineWidth(1);
            gc.strokeLine(padL,y,padL+chartW,y);
            gc.setFill(Color.web("#94a3b8"));
            gc.fillText(String.valueOf((int)(max*i/4.0)),4,y+4);
        }
        double barW=chartW/months.length*0.55, gap=chartW/months.length;
        for (int i=0;i<months.length;i++) {
            double barH=(monthly[i]/(double)max)*chartH;
            double x=padL+i*gap+(gap-barW)/2;
            double y=padT+chartH-barH;
            gc.setFill(Color.web("#3b82f6"));
            gc.fillRoundRect(x,y,barW,barH,6,6);
            gc.setFill(Color.web("#64748b"));
            gc.fillText(months[i],x+barW/2-10,h-8);
        }
    }

    private StackPane makeAvatar(String fullName, double size) {
        Circle circle = new Circle(size/2);
        String[] colors = {"#2563eb","#7c3aed","#db2777","#059669","#d97706","#dc2626"};
        int idx = fullName.isEmpty() ? 0 : Math.abs(fullName.charAt(0) % colors.length);
        circle.setFill(Color.web(colors[idx]));
        String initials = fullName.isBlank() ? "?" :
            (fullName.trim().length() > 1
                ? String.valueOf(fullName.trim().charAt(0)).toUpperCase()
                  + (fullName.trim().contains(" ")
                      ? String.valueOf(fullName.trim().split(" ")[1].charAt(0)).toUpperCase()
                      : "")
                : String.valueOf(fullName.trim().charAt(0)).toUpperCase());
        Label lbl = new Label(initials);
        lbl.setStyle("-fx-font-size:"+(size*0.32)+"px; -fx-font-weight:bold; -fx-text-fill:white;");
        StackPane sp = new StackPane(circle,lbl);
        sp.setPrefSize(size,size); sp.setMinSize(size,size); sp.setMaxSize(size,size);
        return sp;
    }

    private VBox timeBox(String label, String value, String color) {
        VBox b = new VBox(4);
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:"+color+";");
        b.getChildren().addAll(lbl, val); return b;
    }

    private VBox infoField(String label, Label val) {
        VBox b = new VBox(4); b.setPadding(new Insets(12,8,12,8));
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8; -fx-font-weight:bold;");
        val.setStyle("-fx-font-size:14px; -fx-text-fill:#1e293b;");
        Separator sep = new Separator(); sep.setStyle("-fx-opacity:0.5;");
        b.getChildren().addAll(lbl,val,sep); return b;
    }

    private VBox statCard(String title, Label val, String icon, String bg) {
        VBox c = new VBox(6); c.setPadding(new Insets(20));
        c.setStyle("-fx-background-color:white; -fx-background-radius:12;" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:12;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");
        HBox row = new HBox(); row.setAlignment(Pos.CENTER_LEFT);
        VBox texts = new VBox(4);
        Label t = new Label(title); t.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");
        val.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");
        texts.getChildren().addAll(t,val);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label ico = new Label(icon); ico.setStyle("-fx-background-color:"+bg+
            "; -fx-background-radius:10; -fx-font-size:22px; -fx-padding:10 12;");
        row.getChildren().addAll(texts,sp,ico);
        c.getChildren().add(row); return c;
    }

    private Button qaBtn(String text) {
        Button btn = new Button(text); btn.setCursor(Cursor.HAND);
        btn.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btn, Priority.ALWAYS);
        String base = "-fx-background-color:white; -fx-text-fill:#2563eb;" +
            "-fx-border-color:#2563eb; -fx-border-radius:8; -fx-background-radius:8;" +
            "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:10 16;";
        String hover= "-fx-background-color:#eff6ff; -fx-text-fill:#1d4ed8;" +
            "-fx-border-color:#1d4ed8; -fx-border-radius:8; -fx-background-radius:8;" +
            "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:10 16;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private Button navItem(String text, boolean active) {
        Button btn = new Button(text); btn.setMaxWidth(Double.MAX_VALUE); btn.setCursor(Cursor.HAND);
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

    private VBox card() {
        VBox v = new VBox(12); v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color:white; -fx-background-radius:12;" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:12;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");
        return v;
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<T, String> attCol(String title, double w,
            java.util.function.Function<T, String> extractor) {
        TableColumn<T,String> c = new TableColumn<>(title);
        c.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(extractor.apply(d.getValue())));
        c.setMinWidth(w); return c;
    }

    private TableColumn<LeaveRequest,String> lrCol(String title, double w,
            java.util.function.Function<LeaveRequest, String> extractor) {
        TableColumn<LeaveRequest,String> c = new TableColumn<>(title);
        c.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(extractor.apply(d.getValue())));
        c.setMinWidth(w); return c;
    }

    private TableColumn<LeaveRequest,Void> statusBadgeCol() {
        TableColumn<LeaveRequest,Void> c = new TableColumn<>("Status");
        c.setMinWidth(95); c.setSortable(false);
        c.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item,empty);
                if (empty) { setGraphic(null); return; }
                LeaveRequest lr = getTableView().getItems().get(getIndex());
                Label badge = new Label(lr.getStatus());
                badge.setStyle("-fx-background-color:" + switch(lr.getStatus()){
                    case "Approved" -> "#dcfce7; -fx-text-fill:#16a34a;";
                    case "Rejected" -> "#fee2e2; -fx-text-fill:#dc2626;";
                    default         -> "#fef9c3; -fx-text-fill:#ca8a04;";
                } + "-fx-background-radius:12; -fx-font-size:10px;" +
                   "-fx-font-weight:bold; -fx-padding:3 10;");
                setGraphic(badge);
            }
        });
        return c;
    }

    private ColumnConstraints col50() {
        ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50); return cc;
    }

    private Label fLbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#334155;");
        return l;
    }

    // ── Public API ────────────────────────────────────────────
    // show(), hide(), close(), getStage(), setOnCloseRequest() — from BaseView
    public Button getBtnDashboard()        { return btnDashboard; }
    public Button getBtnProfile()          { return btnProfile; }
    public Button getBtnAttendance()       { return btnAttendance; }
    public Button getBtnApplyLeave()       { return btnApplyLeave; }
    public Button getBtnLogout()           { return btnLogout; }
    public Button getBtnMarkAttendance()   { return btnMarkAttendance; }
    public Button getBtnApplyLeaveAction() { return btnApplyLeaveAction; }
    public Button getBtnViewLeaveStatus()  { return btnViewLeaveStatus; }
    public Button getBtnEditProfile()      { return btnEditProfile; }

    public void setTotalAttendance(int n) { if (lblTotalAttendance!=null) lblTotalAttendance.setText(String.valueOf(n)); }
    public void setApprovedLeaves(int n)  { if (lblApprovedLeaves!=null)  lblApprovedLeaves.setText(String.valueOf(n)); }
    public void setPendingLeaves(int n)   { if (lblPendingLeaves!=null)   lblPendingLeaves.setText(String.valueOf(n)); }
}
