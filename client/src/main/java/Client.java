import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

class Client {

    private final Socket socket;
    private final ObjectInputStream reader;
    private final ObjectOutputStream writer;
    private final Marshaller marshaller;

    public Client(String host, int port, Marshaller marshaller) throws IOException {
        this.socket = new Socket(InetAddress.getByName(host), port);
        this.reader = new ObjectInputStream(socket.getInputStream());
        this.writer = new ObjectOutputStream(socket.getOutputStream());
        this.marshaller = marshaller;
    }

    public void send(Object object) throws IOException {
        byte[] bytes = marshaller.marshall(object);
        writer.write(bytes);
        writer.flush();
    }

    public Object receive() throws IOException, ClassNotFoundException {
        byte[] bytes = reader.readAllBytes();
        return marshaller.unmarshall(bytes);
    }

    public void close() throws IOException {
        socket.close();
        reader.close();
        writer.close();
    }

}