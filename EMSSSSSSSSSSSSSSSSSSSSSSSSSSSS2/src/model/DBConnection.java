package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "ems";
    private static final String USER     = "root";
    private static final String PASSWORD = "Pradipradi12@#"; // ← change to your MySQL password

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        Properties p = new Properties();
        p.setProperty("user",                    USER);
        p.setProperty("password",                PASSWORD);
        p.setProperty("useSSL",                  "false");
        p.setProperty("allowPublicKeyRetrieval", "true");
        p.setProperty("serverTimezone",          "UTC");
        return DriverManager.getConnection(URL, p);
    }
}
