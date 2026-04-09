package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Attendance {

    private int       id;
    private int       employeeId;
    private String    employeeName;
    private LocalDate workDate;
    private LocalTime checkIn;
    private LocalTime checkOut;

    public Attendance(int id, int employeeId, String employeeName,
                      LocalDate workDate, LocalTime checkIn, LocalTime checkOut) {
        this.id           = id;
        this.employeeId   = employeeId;
        this.employeeName = employeeName;
        this.workDate     = workDate;
        this.checkIn      = checkIn;
        this.checkOut     = checkOut;
    }

    public int       getId()           { return id; }
    public int       getEmployeeId()   { return employeeId; }
    public String    getEmployeeName() { return employeeName; }
    public LocalDate getWorkDate()     { return workDate; }
    public LocalTime getCheckIn()      { return checkIn; }
    public LocalTime getCheckOut()     { return checkOut; }
    public void      setCheckOut(LocalTime t) { checkOut = t; }

    public String getHoursWorked() {
        if (checkIn == null || checkOut == null) return "--";
        long mins = java.time.Duration.between(checkIn, checkOut).toMinutes();
        if (mins < 0) return "--";
        return String.format("%dh %02dm", mins / 60, mins % 60);
    }
}
