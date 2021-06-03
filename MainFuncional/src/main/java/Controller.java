import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    public VBox primaryStage;
    public Label cpuLabel;
    public Label memLabel;
    public Label timeLabel;
    public static Label staticTimeLabel;
    public static Label staticCpuLabel;
    public static Label staticMemLabel;
    public Button saveButton;
    public static String logFileName = "log.txt";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        staticCpuLabel = cpuLabel;
        staticMemLabel = memLabel;
        staticTimeLabel = timeLabel;

        try {
            File fileName = Paths.get(".").toAbsolutePath().getParent().getParent().resolve("saved.txt").toFile();
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String str;
            while ((str = reader.readLine()) != null) {
                logFileName = str;
            }
            reader.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void saveButtonAction() {
        try {
            File fileName = Paths.get(".").toAbsolutePath().getParent().getParent().resolve("saved.txt").toFile();
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String str;
            while ((str = reader.readLine()) != null) {
                logFileName = str;
            }
            reader.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true));
            writer.append(" ");
            writer.append("Information from Main Functional:");
            writer.append(cpuLabel.getText());
            writer.append(memLabel.getText());
            writer.append(timeLabel.getText());
            writer.append("-----------------------------------");
            writer.append("\r\n");
            writer.flush();
            writer.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
