package model;
// Encapsulation - binding of data and methods in a entity like a capsule
public class User {

    private int    id;
    private String username;
    private String role;
    private String email;

    public User(int id, String username, String role, String email) {
        this.id       = id;
        this.username = username;
        this.role     = role;
        this.email    = email;
    }

    public int    getId()       { return id; }
    public String getUsername() { return username; }
    public String getRole()     { return role; }
    public String getEmail()    { return email; }
}
