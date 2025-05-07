package dto;

import java.io.Serializable;

public class UserRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String command;
    private int userId;
    private String login;
    private String password;
    private String role;
    private boolean block; // ⬅ новое поле

    public UserRequest() {}

    public UserRequest(String command, int userId, String login, String password, String role) {
        this.command = command;
        this.userId = userId;
        this.login = login;
        this.password = password;
        this.role = role;
    }
    public UserRequest(String command) {
        this.command = command;

    }


    // новый конструктор для команд типа блокировки
    public UserRequest(String command, String login, boolean block) {
        this.command = command;
        this.login = login;
        this.block = block;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBlock() {
        return block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "command='" + command + '\'' +
                ", userId=" + userId +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", block=" + block +
                '}';
    }
}
