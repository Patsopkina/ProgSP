// AuthResponse.java
package dto;

import java.io.Serializable;

public class AuthResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String message;
    private String role;

    public AuthResponse(boolean success, String message, String role) {
        this.success = success;
        this.message = message;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getRole() {
        return role;
    }
}
