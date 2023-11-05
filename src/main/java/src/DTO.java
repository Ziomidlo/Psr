package src;

import src.enums.ConnectionType;
import src.enums.OperationType;

import java.io.Serializable;

public class DTO implements Serializable {

    private ConnectionType connectionType;
    private OperationType operation;
    private byte[] data;

    public DTO() {
    }

    public DTO(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public DTO(OperationType operation, byte[] data) {
        this.operation = operation;
        this.data = data;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
