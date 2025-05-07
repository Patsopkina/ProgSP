package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String login;
    private String role;
    private boolean isBlocked;

    public User(int id, String login, String role, boolean isBlocked) {
        this.id = id;
        this.login = login;
        this.role = role;
        this.isBlocked = isBlocked;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getRole() {
        return role;
    }

    public boolean isBlocked() {
        return isBlocked;
    }
}
