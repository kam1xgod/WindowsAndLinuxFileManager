import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class NetworkConnection {
    private Consumer<Serializable> onReceiveCallBack;
    private ConnectionThread connThread = new ConnectionThread();

    public NetworkConnection(Consumer<Serializable> onReceiveCallBack) {
        this.onReceiveCallBack = onReceiveCallBack; //Consumer makes something with object and returns nothing. for a better understanding: it's like a method.
        connThread.setDaemon(true); //setting thread to daemon-thread. background thread that support basic threads.
    }

    public void startConnection() {
        connThread.start(); //starting connection thread.
    }

    public void send(Serializable data) throws Exception {
        connThread.out.writeObject(data); //try to write object in socket.
    }

    public void closeConnection() throws Exception {
        connThread.socket.close(); //closing connection.
    }

    protected abstract boolean isServer(); //bool method for knowing if it's client or server.
    protected abstract String getIP(); //get IP address method. actually we're using 127.0.0.1. but why not.
    protected abstract int getPort(); //get port. 6666 in my case.

    private class ConnectionThread extends Thread {
        private Socket socket;
        private ObjectOutputStream out;

        @Override
        public void run() {
            try (ServerSocket server = isServer() ? new ServerSocket(getPort()) : null; //if socket is server make new server. if not — do nothing.
            Socket socket = isServer() ? server.accept() : new Socket(getIP(), getPort()); //if socket is server call consumer method accept. if not — create socket (client).
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); //create ObjectOutput and Input streams.
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) { //this was declared in try block without needing in finally block. by "()".

                this.socket = socket;
                this.out = out;
                socket.setTcpNoDelay(true); //removing delay on out operations.

                while (true) {
                    Serializable data = (Serializable) in.readObject(); //while cycle that receives data.
                    onReceiveCallBack.accept(data); //call of consumer method accept.
                }
            }
            catch (Exception e) {
                onReceiveCallBack.accept("Connection closed.");
            }
        }
    }
}
