package dto;

import java.io.Serializable;

public class KitchenStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String commandType;

    public KitchenStatusRequest(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
