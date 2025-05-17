package model;

import java.time.LocalDateTime;

public class Feedback  {
    private int id;
    private String employeeLogin;
    private String message;
    private LocalDateTime timestamp;

    public Feedback(String employeeLogin, String message) {
        this.employeeLogin = employeeLogin;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Feedback(int id, String employeeLogin, String message, LocalDateTime timestamp) {
        this.id = id;
        this.employeeLogin = employeeLogin;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getEmployeeLogin() {
        return employeeLogin;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + employeeLogin + ": " + message;
    }
}
