package src;

public class OperationService {

    private static OperationService instance;

    private OperationService() {}

    public synchronized static OperationService getInstance() {
        if (instance == null) new OperationService();
        return instance;
    }

}
