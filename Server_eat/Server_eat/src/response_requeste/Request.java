package response_requeste;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestType;  // Тип запроса (например, "REGISTER", "LOGIN", "BLOCK_USER")
    private Object data;         // Данные запроса (например, данные пользователя)

    // Конструктор
    public Request(String requestType, Object data) {
        this.requestType = requestType;
        this.data = data;
    }

    // Геттеры и сеттеры
    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}