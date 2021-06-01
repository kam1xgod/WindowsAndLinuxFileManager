import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Controller extends Thread implements Initializable {

    public VBox leftTable;
    public VBox rightTable;
    public VBox driverSelection;
    public static String osType = ""; //variable for cross-platform.
    public MenuItem buttonAbout;
    public VBox primaryStage;
    public static VBox staticPrimaryStage;
    public static Process p; //Process p declared here for easy access from Main class where process should be killed on app closing.
    public Stage aboutStage;
    public Button commandLineButton;
    public Button calcButton;
    public static boolean isCanceled = false; //bool variable for data transfer threads.
    public TextField nameTextField;
    public Button newButton;
    public Button createButton;
    public static String logFileName = "log.txt"; //string variable for knowing log file name. "log.txt" is a temp (or default) name.
    boolean isClicked = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        staticPrimaryStage = primaryStage; //create static reference for stage.

        //declare logFileName variable which contains current name of log file.

        try {
            File savedFilePath = Paths.get(".").resolve("saved.txt").toAbsolutePath().toFile();
            BufferedReader reader = new BufferedReader(new FileReader(savedFilePath));
            String str;
            while ((str = reader.readLine()) != null) {
                logFileName = str;
            }
            reader.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        if (!Files.exists(Paths.get(".", logFileName))) {
            try {
                Files.createFile(Paths.get(".", logFileName));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        logFileClearing(); //clearing log file on start.

        //checks if it's Linux or Windows and changing string variable osType.
        osType = SystemUtils.IS_OS_LINUX ? "Linux" : "Windows";

        //adding context menus for both of tables.
        leftTable.setOnContextMenuRequested(event -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem newMenuItem = new MenuItem("New");
            MenuItem openMenuItem = new MenuItem("Open");
            MenuItem copyMenuItem = new MenuItem("Copy");
            MenuItem moveMenuItem = new MenuItem("Move");
            MenuItem deleteMenuItem = new MenuItem("Delete");

            newMenuItem.setOnAction(event1 -> buttonNewAction());
            openMenuItem.setOnAction(event2 -> buttonOpenAction());
            copyMenuItem.setOnAction(event3 -> buttonCopyAction());
            moveMenuItem.setOnAction(event4 -> buttonMoveAction());
            deleteMenuItem.setOnAction(event5 -> buttonDeleteAction());

            contextMenu.getItems().addAll(openMenuItem, copyMenuItem, moveMenuItem, deleteMenuItem);
            PanelController leftPanelController = (PanelController) leftTable.getProperties().get("control");
            leftPanelController.filesTableView.setContextMenu(contextMenu);
            contextMenu.show(leftTable, event.getScreenX(), event.getScreenY());
        });

        rightTable.setOnContextMenuRequested(event -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem newMenuItem = new MenuItem("New");
            MenuItem openMenuItem = new MenuItem("Open");
            MenuItem copyMenuItem = new MenuItem("Copy");
            MenuItem moveMenuItem = new MenuItem("Move");
            MenuItem deleteMenuItem = new MenuItem("Delete");

            newMenuItem.setOnAction(event1 -> buttonNewAction());
            openMenuItem.setOnAction(event2 -> buttonOpenAction());
            copyMenuItem.setOnAction(event3 -> buttonCopyAction());
            moveMenuItem.setOnAction(event4 -> buttonMoveAction());
            deleteMenuItem.setOnAction(event5 -> buttonDeleteAction());

            contextMenu.getItems().addAll(openMenuItem, copyMenuItem, moveMenuItem, deleteMenuItem);
            PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");
            rightTableController.filesTableView.setContextMenu(contextMenu);
            contextMenu.show(rightTable, event.getScreenX(), event.getScreenY());
        });

        Main.stage.setOnCloseRequest(event -> Platform.runLater(() -> {
            if (aboutStage != null && aboutStage.isShowing()) {
                aboutStage.close();
            }
            if (p != null && p.isAlive()) {
                p.destroy();
            }
        }));

        //button "Go to" on Drive selection.

        DriveSelectionController driveController = (DriveSelectionController) driverSelection.getProperties().get("driveControl");
        driveController.gotoButton.setOnMouseClicked(event -> {
            Path path = driveController.getCurDrive();
            PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
            PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");
            leftTableController.updateTableView(path);
            rightTableController.updateTableView(path);
        });

        //Drive selection item double click.

        driveController.drivesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Path path = driveController.getCurDrive();
                PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
                PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");
                leftTableController.updateTableView(path);
                rightTableController.updateTableView(path);
            }
        });
    }

    public void buttonExitAction() {
        Platform.exit();
    }

    public void buttonCopyAction() { //"Copy" button on main screen.
        try {
            PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
            PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");

            PanelController sourceTable = null;
            PanelController destinationTable = null;

            if (leftTableController.filesTableView.isFocused()) {
                sourceTable = leftTableController;
                destinationTable = rightTableController;
            }

            if (rightTableController.filesTableView.isFocused()) {
                sourceTable = rightTableController;
                destinationTable = leftTableController;
            }

            if (sourceTable != null && sourceTable.filesTableView.getSelectionModel().getSelectedItem() != null) {
                if (sourceTable.getSelFileName().equals("System") || sourceTable.getSelFileName().equals("Trash") ||
                        sourceTable.getCurPath().contains("System") || sourceTable.getCurPath().contains("Trash")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You can't work with this folder.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            if (destinationTable != null && destinationTable.filesTableView.getSelectionModel().getSelectedItem() != null) {
                if (destinationTable.getSelFileName().equals("System") || destinationTable.getSelFileName().equals("Trash") ||
                        destinationTable.getCurPath().contains("System") || destinationTable.getCurPath().contains("Trash")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You can't work with this folder.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            if (sourceTable != null) {
                File sourcePath = Paths.get(sourceTable.getCurPath()).resolve(sourceTable.getSelFileName()).toFile();
                File destPath = Paths.get(destinationTable.getCurPath()).resolve(sourceTable.getSelFileName()).toFile();

                if (sourcePath.isDirectory()) {
                    FileUtils.copyDirectory(sourcePath, destPath);
                    sourceTable.updateTableView(Paths.get(sourceTable.getCurPath()));
                    destinationTable.updateTableView(Paths.get(destinationTable.getCurPath()));
                    return;
                }
                Files.copy(sourcePath.toPath(), destPath.toPath());
                sourceTable.updateTableView(Paths.get(sourceTable.getCurPath()));
                destinationTable.updateTableView(Paths.get(destinationTable.getCurPath()));
            }
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No files selected.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void buttonOpenAction() { //"Open" button on main screen.
        PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
        PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");

        try {

            PanelController curTable = null;

            if (leftTableController.filesTableView.isFocused()) {
                curTable = leftTableController;
            }

            if (rightTableController.filesTableView.isFocused()) {
                curTable = rightTableController;
            }

            Path path = Paths.get(curTable.pathField.getText()).resolve(curTable.filesTableView.getSelectionModel().getSelectedItem().getFileName());
            if (Files.isDirectory(path)) {
                curTable.updateTableView(path);
                return;
            }

            try {
                BufferedWriter logWriter = new BufferedWriter(new FileWriter(Paths.get(".", "log.txt").toString(), true));
                logWriter.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                logWriter.append(" ");
                logWriter.append(curTable.filesTableView.getSelectionModel().getSelectedItem().getFileName());
                logWriter.append("\r\n");
                logWriter.close();
                Desktop.getDesktop().open(path.toFile());
            } catch (IOException exception) {
                exception.printStackTrace();
            }

        } catch (NullPointerException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No files selected.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void buttonRefreshAction() { //Menu button "Refresh"
        PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
        PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");

        leftTableController.updateTableView(Paths.get(leftTableController.pathField.getText()));
        rightTableController.updateTableView(Paths.get(rightTableController.pathField.getText()));
    }

    public void buttonDeleteAction() { //"Delete" button on main screen.
        try {
            PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
            PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");

            PanelController sourceTable = null;
            PanelController secondTable = null;

            if (leftTableController.filesTableView.isFocused()) {
                sourceTable = leftTableController;
                secondTable = rightTableController;
            }

            if (rightTableController.filesTableView.isFocused()) {
                sourceTable = rightTableController;
                secondTable = leftTableController;
            }

            if (sourceTable.getSelFileName().equals("System") || sourceTable.getSelFileName().equals("Trash") ||
                    sourceTable.getCurPath().contains("System") || sourceTable.getCurPath().contains("Trash")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You can't work with this folder.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            Path sourcePath = Paths.get(sourceTable.getCurPath(), sourceTable.getSelFileName());
            Path destPath = Paths.get(".").normalize().toAbsolutePath().getParent().resolve("Trash");

            try {
                Files.move(sourcePath, destPath.resolve(sourcePath.getFileName()), REPLACE_EXISTING);
                sourceTable.updateTableView(Paths.get(sourceTable.getCurPath()));
                secondTable.updateTableView(Paths.get(secondTable.getCurPath()));
            } catch (IOException exception) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to move selected file into trash.", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (NullPointerException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No files selected.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void buttonFullDeleteAction() { //"Full delete" button on main screen.
        try {
            PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
            PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");

            PanelController sourceTable = null;
            PanelController secondTable = null;

            if (leftTableController.filesTableView.isFocused()) {
                sourceTable = leftTableController;
                secondTable = rightTableController;
            }

            if (rightTableController.filesTableView.isFocused()) {
                sourceTable = rightTableController;
                secondTable = leftTableController;
            }

            if (sourceTable.getSelFileName().equals("System") || sourceTable.getSelFileName().equals("Trash") ||
                    sourceTable.getCurPath().contains("System")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You can't work with this folder.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            Path sourcePath = Paths.get(sourceTable.getCurPath(), sourceTable.getSelFileName());
            try {
                Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(
                            Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
                sourceTable.updateTableView(Paths.get(sourceTable.getCurPath()));
                secondTable.updateTableView(Paths.get(secondTable.getCurPath()));
            } catch (IOException exception) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Unable to move selected file into trash.", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (NullPointerException exception) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No files selected.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void buttonMoveAction() { //"Move" button on main screen.
        try {
            PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
            PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");

            PanelController sourceTable = null;
            PanelController destinationTable = null;

            if (leftTableController.filesTableView.isFocused()) {
                sourceTable = leftTableController;
                destinationTable = rightTableController;
            }

            if (rightTableController.filesTableView.isFocused()) {
                sourceTable = rightTableController;
                destinationTable = leftTableController;
            }

            if (sourceTable != null && sourceTable.filesTableView.getSelectionModel().getSelectedItem() != null) {
                if (sourceTable.getSelFileName().equals("System") || sourceTable.getSelFileName().equals("Trash") ||
                        sourceTable.getCurPath().contains("System") || sourceTable.getCurPath().contains("Trash")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You can't work with this folder.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            if (destinationTable != null && destinationTable.filesTableView.getSelectionModel().getSelectedItem() != null) {
                if (destinationTable.getSelFileName().equals("System") || destinationTable.getSelFileName().equals("Trash") ||
                        destinationTable.getCurPath().contains("System") || destinationTable.getCurPath().contains("Trash")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "You can't work with this folder.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            if (sourceTable != null) {
                File sourcePath = Paths.get(sourceTable.getCurPath()).resolve(sourceTable.getSelFileName()).toFile();
                File destPath = Paths.get(destinationTable.getCurPath()).resolve(sourceTable.getSelFileName()).toFile();

                if (sourcePath.isDirectory()) {
                    FileUtils.moveDirectory(sourcePath, destPath);
                    sourceTable.updateTableView(Paths.get(sourceTable.getCurPath()));
                    destinationTable.updateTableView(Paths.get(destinationTable.getCurPath()));
                    return;
                }
                Files.move(sourcePath.toPath(), destPath.toPath());
                sourceTable.updateTableView(Paths.get(sourceTable.getCurPath()));
                destinationTable.updateTableView(Paths.get(destinationTable.getCurPath()));
            }
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No files selected.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void buttonAboutAction() { //Menu button "About".
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/about.fxml")); //creating new scene from .fxml file.
            Parent root = loader.load();
            aboutStage = new Stage();
            aboutStage.setScene(new Scene(root));
            aboutStage.setTitle("About");
            aboutStage.setResizable(false);
            aboutStage.show();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void buttonMainFuncAction() throws IOException { //Menu button "Main functional.
        //Starting new process from .jar file;
        //Starting threads that will transfer data to subprocess. They're synchronised by semaphore.
        //semaphore for synchronised data transfer.
        Semaphore sem;
        if (osType.equals("Windows")) {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", Paths.get(".").toAbsolutePath().resolve("MainFuncional\\out\\artifacts\\MainFuctional_jar").toString() + "\\MainFuctional.jar");

            p = pb.start();

            ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(3); //for scheduled thread start because of "Connection closed".

            sem = new Semaphore(1);

            WritingTimeThread timeThread = new WritingTimeThread("test", sem);
            WritingCPUThread cpuThread = new WritingCPUThread("test", sem);
            WritingMemThread memThread = new WritingMemThread("test", sem);
            ses.schedule(timeThread, 3, TimeUnit.SECONDS);
            ses.schedule(cpuThread, 4, TimeUnit.SECONDS);
            ses.schedule(memThread, 5, TimeUnit.SECONDS);

        } else {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", Paths.get(".").normalize().toAbsolutePath().resolve("MainFuncional/out/artifacts/MainFuctional_jar") + "/MainFuctional.jar");
            p = pb.start();
            ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(3); //for scheduled thread start because of "Connection closed".
            sem = new Semaphore(1);
            WritingTimeThread timeThread = new WritingTimeThread("test", sem);
            WritingCPUThread cpuThread = new WritingCPUThread("test", sem);
            WritingMemThread memThread = new WritingMemThread("test", sem);
            timeThread.setDaemon(true);
            cpuThread.setDaemon(true);
            memThread.setDaemon(true);
            ses.schedule(timeThread, 7, TimeUnit.SECONDS);
            ses.schedule(cpuThread, 8, TimeUnit.SECONDS);
            ses.schedule(memThread, 9, TimeUnit.SECONDS);
        }

    }

    public void cmdButtonAction() throws IOException { //open cmd or terminal.
        if (osType.equals("Windows")) {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(Paths.get(".", logFileName).toString(), true));
            logWriter.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            logWriter.append(" ");
            logWriter.append("cmd.exe");
            logWriter.append("\r\n");
            logWriter.close();
            Desktop.getDesktop().open(Paths.get("C:\\WINDOWS\\system32\\cmd.exe").toFile()); //default location of cmd on Windows.
            return;
        }
        BufferedWriter logWriter = new BufferedWriter(new FileWriter(Paths.get(".", logFileName).toString(), true));
        logWriter.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logWriter.append(" ");
        logWriter.append("terminal");
        logWriter.append("\r\n");
        logWriter.close();
        ProcessBuilder terminal = new ProcessBuilder("gnome-terminal"); //linux terminal command to start new terminal session. note: works only with gnome shell.
        terminal.start();
    }

    public void calcButtonAction() throws IOException { //open calculator.
        if (osType.equals("Windows")) {
            BufferedWriter logWriter = new BufferedWriter(new FileWriter(Paths.get(".", logFileName).toString(), true));
            logWriter.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            logWriter.append(" ");
            logWriter.append("calc.exe");
            logWriter.append("\r\n");
            logWriter.close();
            Desktop.getDesktop().open(Paths.get("C:\\Windows\\System32\\calc.exe").toFile()); //default location of calculator on Windows.
            return;
        }

        BufferedWriter logWriter = new BufferedWriter(new FileWriter(Paths.get(".", logFileName).toString(), true));
        logWriter.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logWriter.append(" ");
        logWriter.append("gnome-calculator");
        logWriter.append("\r\n");
        logWriter.close();
        ProcessBuilder calc = new ProcessBuilder("gnome-calculator"); //linux terminal command to start calculator. note: works only with gnome shell.
        calc.start();
    }

    public void logFileClearing() { //clearing .txt file on each new session.
        try {
            Files.delete(Paths.get(".", logFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Files.createFile(Paths.get(".", logFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeTheme(String theme) {
        staticPrimaryStage.getStylesheets().clear();
        staticPrimaryStage.getStylesheets().add(theme);
    }

    public void buttonNewAction() {
        if (!isClicked) {
            nameTextField.setVisible(true);
            newButton.setText("Cancel");
            createButton.setVisible(true);
            isClicked = true;

            return;
        }
        nameTextField.setVisible(false);
        newButton.setText("New");
        createButton.setVisible(false);
        isClicked = false;
    }

    public void createButtonAction() throws IOException {
        if (nameTextField.getLength() < 1) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Insert name, please.", ButtonType.OK);
            alert.showAndWait();
        }

        PanelController leftTableController = (PanelController) leftTable.getProperties().get("control");
        PanelController rightTableController = (PanelController) rightTable.getProperties().get("control");
        PanelController curTable;
        PanelController secondTable;

        if (!leftTableController.filesTableView.isFocused() && !rightTableController.filesTableView.isFocused()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "select table first.", ButtonType.OK);
            alert.showAndWait();
            return;
        }


        if (leftTableController.filesTableView.isFocused()) {
            curTable = leftTableController;
            secondTable = rightTableController;
            Path path = Paths.get(curTable.getCurPath());

            if (nameTextField.getText().split("\\.").length == 2) {
                Files.createFile(path).resolve(nameTextField.getText());
                curTable.updateTableView(path);
                secondTable.updateTableView(Paths.get(secondTable.getCurPath()));
                return;
            }

            Files.createDirectory(path.resolve(nameTextField.getText()));
            curTable.updateTableView(path);
            secondTable.updateTableView(Paths.get(secondTable.getCurPath()));

            nameTextField.setVisible(false);
            newButton.setText("New");
            createButton.setVisible(false);
            isClicked = false;
        }

        if (rightTableController.filesTableView.isFocused()) {
            curTable = rightTableController;
            secondTable = leftTableController;
            Path path = Paths.get(curTable.getCurPath());

            if (nameTextField.getText().split("\\.").length == 2) {
                Files.createFile(path.resolve(nameTextField.getText()));
                curTable.updateTableView(path);
                secondTable.updateTableView(Paths.get(secondTable.getCurPath()));
                return;
            }


            Files.createDirectory(path.resolve(nameTextField.getText()));
            curTable.updateTableView(path);
            secondTable.updateTableView(Paths.get(secondTable.getCurPath()));

            nameTextField.setVisible(false);
            newButton.setText("New");
            createButton.setVisible(false);
            isClicked = false;
        }
    }
}



