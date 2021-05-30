import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class App extends Application {

    static Stage stage;

    public static NetworkConnectionClientSide connection = createClient();

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("main functional");
        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.show();

        stage.setOnCloseRequest(event -> {
            try {
                stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });
    }

    @Override
    public void init() throws Exception {
        connection.startConnection();

    }

    public static Client createClient() {
        return new Client("127.0.0.1", 6666, data -> {
            if (data.toString().startsWith("t")) {
                Platform.runLater(() -> MainFuc.staticTimeLabel.setText("Current local time: " + data.toString().replace('t', ' ')));
                return;
            }
            if (data.toString().startsWith("m")) {
                Platform.runLater(() -> MainFuc.staticMemLabel.setText("Virtual memory usage:  " + data.toString().replace('m', ' ')));
                return;
            }
            Platform.runLater(() -> MainFuc.staticCpuLabel.setText("Cpu usage time: " + data.toString().replace('p', ' ')));
        });
    }

    @Override
    public void stop() throws Exception {
        connection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
