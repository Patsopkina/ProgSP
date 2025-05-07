package dto;

import model.User;
import java.util.List;


import java.io.Serializable;


public class UserResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<User> users;
    // Конструктор с 3 параметрами
    public UserResponse(boolean success, String message, List<User> users) {
        this.success = success;
        this.message = message;
        this.users = users;
    }
    public UserResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

public UserResponse (){

}
    // Геттеры и сеттеры
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
