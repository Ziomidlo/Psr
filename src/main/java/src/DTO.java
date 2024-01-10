package src;

import src.enums.OperationType;

import java.io.Serializable;

/***
 * Obiekt DTO uzywany do komunikacji klient-serwer, serwer-serwer.
 * Zawiera wykonywana operacje, klucz przypisany do pliku, przekazywane dane (plik),
 * oraz flage odpowiedzialna za mozliwosc ustalenia czy zadanie wyslane zostalo z klienta
 * czy z innej instancji.
 */
public class DTO implements Serializable {

    private OperationType operation;
    private String key;
    private byte[] data;
    private boolean isDataFromAnotherInstance;

    public DTO() {
    }

    public DTO(OperationType operation) {
        this.operation = operation;
    }

    public DTO(OperationType operation, String key) {
        this.operation = operation;
        this.key = key;
    }

    public DTO(OperationType operation, String key, boolean isDataFromAnotherInstance) {
        this.operation = operation;
        this.key = key;
        this.isDataFromAnotherInstance = isDataFromAnotherInstance;
    }

    public DTO(OperationType operation, byte[] data) {
        this.operation = operation;
        this.data = data;
    }

    public DTO(OperationType operation, String key, byte[] data) {
        this.operation = operation;
        this.key = key;
        this.data = data;
    }

    public DTO(OperationType operation, String key, byte[] data, boolean isDataFromAnotherInstance) {
        this.operation = operation;
        this.key = key;
        this.data = data;
        this.isDataFromAnotherInstance = isDataFromAnotherInstance;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isDataFromAnotherInstance() {
        return isDataFromAnotherInstance;
    }

    public void setDataFromAnotherInstance(boolean dataFromAnotherInstance) {
        isDataFromAnotherInstance = dataFromAnotherInstance;
    }
}
