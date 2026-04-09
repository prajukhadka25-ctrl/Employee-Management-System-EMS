package model;

import java.time.LocalDate;

public class LeaveRequest {

    private int       id;
    private int       employeeId;
    private String    employeeName;
    private String    leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String    reason;
    private String    status;
    private String    adminNote;

    // Full constructor (from DB join with employee name)
    public LeaveRequest(int id, int employeeId, String employeeName,
                        String leaveType, LocalDate startDate, LocalDate endDate,
                        String reason, String status, String adminNote) {
        this.id           = id;
        this.employeeId   = employeeId;
        this.employeeName = employeeName;
        this.leaveType    = leaveType;
        this.startDate    = startDate;
        this.endDate      = endDate;
        this.reason       = reason;
        this.status       = status;
        this.adminNote    = adminNote;
    }

    public int       getId()             { return id; }
    public int       getEmployeeId()     { return employeeId; }
    public String    getEmployeeName()   { return employeeName; }
    public String    getLeaveType()      { return leaveType; }
    public LocalDate getStartDate()      { return startDate; }
    public LocalDate getEndDate()        { return endDate; }
    public String    getReason()         { return reason != null ? reason : ""; }
    public String    getStatus()         { return status; }
    public String    getAdminNote()      { return adminNote != null ? adminNote : ""; }

    public void setStatus(String s)    { status    = s; }
    public void setAdminNote(String n) { adminNote = n; }

    public String getDateRange() {
        return startDate + " to " + endDate;
    }

    public long getDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
