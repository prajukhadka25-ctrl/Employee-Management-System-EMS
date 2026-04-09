package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {

    private int        id;
    private int        userId;   // links to users.id for username lookup/update
    private String     firstName;
    private String     lastName;
    private String     email;
    private String     department;
    private String     position;
    private BigDecimal salary;
    private LocalDate  hireDate;
// Method overloading. 
    // Constructor 1 — new employee (no id yet)  — METHOD OVERLOADING
    public Employee(String firstName, String lastName, String email,
                    String department, String position,
                    BigDecimal salary, LocalDate hireDate) {
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.email      = email;
        this.department = department;
        this.position   = position;
        this.salary     = salary;
        this.hireDate   = hireDate;
    }

    // Constructor 2 — loading from DB (has id)  — METHOD OVERLOADING
    public Employee(int id, String firstName, String lastName, String email,
                    String department, String position,
                    BigDecimal salary, LocalDate hireDate) {
        this(firstName, lastName, email, department, position, salary, hireDate);
        this.id = id;
    }

    public int        getId()         { return id; }
    public int        getUserId()     { return userId; }
    public void       setUserId(int v){ userId = v; }
    public String     getFirstName()  { return firstName; }
    public String     getLastName()   { return lastName; }
    public String     getFullName()   { return firstName + " " + lastName; }
    public String     getEmail()      { return email; }
    public String     getDepartment() { return department; }
    public String     getPosition()   { return position; }
    public BigDecimal getSalary()     { return salary; }
    public LocalDate  getHireDate()   { return hireDate; }

    public void setFirstName(String v)   { firstName  = v; }
    public void setLastName(String v)    { lastName   = v; }
    public void setEmail(String v)       { email      = v; }
    public void setDepartment(String v)  { department = v; }
    public void setPosition(String v)    { position   = v; }
    public void setSalary(BigDecimal v)  { salary     = v; }
    public void setHireDate(LocalDate v) { hireDate   = v; }
}
