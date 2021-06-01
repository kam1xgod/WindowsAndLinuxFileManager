import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class About implements Initializable {

    public VBox aboutScene;
    public Label osLabel;
    public TextField logFileNameTextField;
    public RadioButton mintButton;
    public RadioButton darkButton;
    public RadioButton orangeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ToggleGroup tg = new ToggleGroup();
        mintButton.setToggleGroup(tg);
        darkButton.setToggleGroup(tg);
        orangeButton.setToggleGroup(tg);
        mintButton.setSelected(true);

        osLabel.setText(Controller.osType);
    }

    public void saveLogFileName() throws Exception {
        String oldLogFileName = "log.txt"; //just a default log file name.
        File savedFilePath = Paths.get(".").resolve("saved.txt").toAbsolutePath().toFile(); //using file to store previous log file name.
        BufferedReader reader = new BufferedReader(new FileReader(savedFilePath)); //creating buffered reader and writer.
        BufferedWriter writer = new BufferedWriter(new FileWriter(savedFilePath));
        String str;
        while ((str = reader.readLine()) != null) {
             oldLogFileName = str; //reading from file.
        }
        reader.close(); //closing reader.
        if (!logFileNameTextField.getText().equals(oldLogFileName)) { //if name isn't the same.
            Controller.logFileName = logFileNameTextField.getText() + ".txt"; //change variable in Controller class.
            Files.copy(Paths.get(".", oldLogFileName), Paths.get(".", Controller.logFileName)); //coping log file.
            Files.delete(Paths.get(".", oldLogFileName)); //deleting previous log file.
            writer.write(Controller.logFileName); //writing new log file name into file.
            writer.close(); //closing writer.
            return;
        }
        Alert alert = new Alert(Alert.AlertType.WARNING, "this is the old name of log file."); //if name is the same.
        alert.showAndWait();
    }

    public void mintThemeButtonAction() {
        aboutScene.getStylesheets().clear();
        aboutScene.getStylesheets().add("/mint.css");
        Controller.changeTheme("/mint.css");
    }

    public void darkThemeButtonAction() {
        aboutScene.getStylesheets().clear();
        aboutScene.getStylesheets().add("/dark.css");
        Controller.changeTheme("/dark.css");
    }

    public void orangeThemeButtonAction() {
        aboutScene.getStylesheets().clear();
        aboutScene.getStylesheets().add("/orange.css");
        Controller.changeTheme("/orange.css");
    }
}
