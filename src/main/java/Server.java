import java.io.Serializable;
import java.util.function.Consumer;

public class Server extends NetworkConnection {

    private int port;

    public Server(int port, Consumer<Serializable> onReceiveCallBack) {
        super(onReceiveCallBack);
        this.port = port;
    }

    @Override
    protected boolean isServer() {
        return true;
    } //returns true cuz it's server.

    @Override
    protected String getIP() {
        return null;
    } //returns null cuz we don't need it here.

    @Override
    protected int getPort() {
        return port;
    } //return current port. 6666.
}
