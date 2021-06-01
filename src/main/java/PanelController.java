import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.*;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {
    @FXML
    TextField pathField;
    @FXML
    TableView<FileInfo> filesTableView = new TableView<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //creating table column which will show type of file.
        TableColumn<FileInfo, String> fileTypeCol = new TableColumn<>();
        fileTypeCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeCol.setPrefWidth(30);

        //creating table column which will show file name.
        TableColumn<FileInfo, String> fileNameCol = new TableColumn<>("File name");
        fileNameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameCol.setPrefWidth(90);

        //creating table column which will show size of file.
        TableColumn<FileInfo, Long> fileSizeCol = new TableColumn<>("File size");
        fileSizeCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeCol.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    } else if (item == -2L) {
                        text = "[TRASH]";
                    }
                    setText(text);
                }
            }
        });
        fileSizeCol.setPrefWidth(90);

        //creating column which will show last modified date.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateCol = new TableColumn<>("Last modified");
        fileDateCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateCol.setPrefWidth(120);

        //adding all created columns on columns collection.
        filesTableView.getColumns().addAll(fileTypeCol, fileNameCol, fileSizeCol, fileDateCol);
        //sorting by file type. directories always in top.
        filesTableView.getSortOrder().add(fileTypeCol);

        filesTableView.setOnMouseClicked(event -> { //event for double mouse click.
            if(event.getClickCount() == 2) {
                Path path = Paths.get(pathField.getText()).resolve(filesTableView.getSelectionModel().getSelectedItem().getFileName());
                if(Files.isDirectory(path)) {
                    updateTableView(path); //go into folder.
                }
                else {
                    try {
                        BufferedWriter logWriter = new BufferedWriter(new FileWriter(Paths.get(".",Controller.logFileName).toString(), true));
                        logWriter.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        logWriter.append(" ");
                        logWriter.append(filesTableView.getSelectionModel().getSelectedItem().getFileName());
                        logWriter.append("\r\n");
                        logWriter.close();
                        Desktop.getDesktop().open(path.toFile()); //open file with default app.
                    } catch (IOException exception) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Can't open this app.", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
            }
        });

        updateTableView(Paths.get(".").normalize().toAbsolutePath().getParent());
    }

    public void updateTableView(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString()); //setting path in path field.
            filesTableView.getItems().clear(); //clearing table.
            filesTableView.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList())); //adding files in path folder using FileInfo as a map.
            filesTableView.sort(); //sorting table.
            filesTableView.getSelectionModel().selectFirst(); //selecting first element of table.
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Can't get access to files. Files list wasn't updated.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void buttonPathBackAction() {
        Path upperPath = Paths.get(pathField.getText()).getParent(); //get parent folder of current directory.
        Path root = Paths.get(".").normalize().toAbsolutePath().getParent().getParent(); //root folder. we can't go upper.
        if (!upperPath.toString().equals(root.toString())) { //if after click we would be lower than root folder update table view.
            updateTableView(upperPath);
        }
    }

    public String getSelFileName() {
        return filesTableView.getSelectionModel().getSelectedItem().getFileName();
    } //getting name of selected item (folder actually.

    public String getCurPath() {
        return pathField.getText();
    } //getting current path from path field.

    public void buttonUpdateClick() { //updating table view. after some actions actually.
        Path path = Paths.get(pathField.getText());
        if(Files.isDirectory(path)) {
            updateTableView(path);
        }
    }
}
