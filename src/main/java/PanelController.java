import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.input.ContextMenuEvent;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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
        fileSizeCol.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
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
            };
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

    public void buttonOpenAction() {
        Path path = Paths.get(pathField.getText()).resolve(filesTableView.getSelectionModel().getSelectedItem().getFileName());
        if(Files.isDirectory(path)) {
            updateTableView(path);
        }
        else {
            try {
                BufferedWriter logWriter = new BufferedWriter(new FileWriter(Paths.get(".","log.txt").toString(), true));
                logWriter.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                logWriter.append(" ");
                logWriter.append(filesTableView.getSelectionModel().getSelectedItem().getFileName());
                logWriter.append("\r\n");
                logWriter.close();
                Desktop.getDesktop().open(path.toFile());
            } catch (IOException exception) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Can't open this app.", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    public void buttonDeleteAction() {
        Path path = Paths.get(getCurPath());

        if(path.resolve(getSelFileName()).toString().equals("System") || path.resolve(getSelFileName()).toString().equals("Trash") ||
                path.resolve(getSelFileName()).toString().contains("System") || path.resolve(getSelFileName()).toString().contains("Trash")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "You can't work with this folder.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if(!getSelFileName().isEmpty()) {
            Path sourcePath = path.resolve(getSelFileName());
            Path destPath = Paths.get(".").normalize().toAbsolutePath().getParent().resolve("Trash");

            try {
                Files.move(sourcePath, destPath.resolve(sourcePath.getFileName()), REPLACE_EXISTING);
                this.updateTableView(path);
            } catch (IOException exception) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to move selected file into trash.", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    public void buttonUpdateClick() { //updating table view. after some actions actually.
        Path path = Paths.get(pathField.getText());
        if(Files.isDirectory(path)) {
            updateTableView(path);
        }
    }

    public void buttonCopyAction() {

    }

    public void buttonMoveAction() {

    }

    static void setAllChildrendTraversable(boolean traversable, ObservableList<Node> childs) {
        for(Node node : childs) {
            if(node instanceof Parent) setAllChildrendTraversable(traversable, ((Parent) node).getChildrenUnmodifiable());
            node.setFocusTraversable(traversable);
        }
    }
}
