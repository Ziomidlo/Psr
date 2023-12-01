package src;

import src.enums.OperationType;

import java.io.Serializable;

public class DTO implements Serializable {

    private OperationType operation;
    private byte[] data;

    public DTO() {
    }

    public DTO(OperationType operation) {
        this.operation = operation;
    }

    public DTO(OperationType operation, byte[] data) {
        this.operation = operation;
        this.data = data;
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
