import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    static Stage stage;
    public static NetworkConnection connection = createServer(); //creating object of NetworkConnection class.

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        primaryStage.setTitle("kami's file manager kekw");
        primaryStage.setScene(new Scene(root, 900, 825));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            cancel();
            if (Controller.p != null) {
                try {
                    stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Platform.exit();
        });
    }

    public void init() { //overwrite of initialization method.
        connection.startConnection();
    }

    public static Server createServer() {
        return new Server(6666, null);
    } //creates Server on port 6666.

    public void stop() throws Exception {
        connection.closeConnection(); //close connection with sockets.
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void cancel() {
        Controller.isCanceled = true;
    } //variable using for threads to stop work.
}
