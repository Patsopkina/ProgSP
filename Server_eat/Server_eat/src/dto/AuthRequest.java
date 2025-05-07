// AuthRequest.java
package dto;

import java.io.Serializable;

public class AuthRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String command; // "login" или "register"
    private String login;
    private String password;

    public AuthRequest(String command, String login, String password) {
        this.command = command;
        this.login = login;
        this.password = password;
    }

    public String getCommand() {
        return command;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
