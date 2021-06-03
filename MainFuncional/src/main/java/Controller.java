import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    public VBox primaryStage;
    public Label cpuLabel;
    public Label memLabel;
    public Label timeLabel;
    public static Label staticTimeLabel;
    public static Label staticCpuLabel;
    public static Label staticMemLabel;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        staticCpuLabel = cpuLabel;
        staticMemLabel = memLabel;
        staticTimeLabel = timeLabel;

        //change to labels. semaphores in FM and here is just an if statements.
    }
}
