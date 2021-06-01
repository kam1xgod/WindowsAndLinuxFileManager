import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class DriveSelectionController implements Initializable {
    @FXML
    public ListView drivesListView;
    public Button updateDrivesButton;
    public Button gotoButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        updateDrivesListView(); //update list on start.

        updateDrivesButton.setOnMouseClicked(event -> updateDrivesListView()); //update button action.
    }

    public void updateDrivesListView() {
        drivesListView.getItems().clear(); //clearing list items.
        drivesListView.getItems().add("Root"); //adding root folder of file manager.
        if (Controller.osType.equals("Windows")) { //is it's Windows-based OS.
            for (File drive : File.listRoots()) { //getting add drives.
                if (!drive.toString().contains("C") && !drive.toString().contains("D")) { //except "D" and "C".
                    drivesListView.getItems().add(drive.toString()); //adding drives to list.
                }
            }
        }

        if (Controller.osType.equals("Linux")) { //is it linux-based OS.
            try {
                //getting all drives from /media/ folder. in linux it's the folder where all removable drives stored.
                for (Path drive : Files.list(Paths.get("/media/").toAbsolutePath().normalize()).toArray(size -> new Path[size])) {
                    drivesListView.getItems().add(drive.toString()); //adding them in list.
                }
            } catch (IOException exception) {} // that's okay.
        }
    }

    public Path getCurDrive() { //get current selected drive.
        if(!drivesListView.getSelectionModel().getSelectedItem().toString().equals("Root")) {
            return Paths.get(drivesListView.getSelectionModel().getSelectedItem().toString()).toAbsolutePath();
        }
        return Paths.get(".").toAbsolutePath().getParent().getParent();
    }
}
