package client;

import client.model.FTPOperations;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class Controller {
    @FXML
    private Button deleteButton;
    @FXML
    private Button uploadFileButton;
    @FXML
    private Button downloadFileButton;
    @FXML
    private Button refreshButton;
    @FXML
    private MenuItem connectMenuItem;
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField bottomTextField;
    @FXML
    private Pane mainPanel;

    private FTPOperations ftpOperations;

    @FXML
    private void initialize() {
        ftpOperations = new FTPOperations();
    }

    @FXML
    private void connectToServer(ActionEvent actionEvent) {
        if (ftpOperations.connect()) {
            refreshFilesList(actionEvent);

            mainPanel.setVisible(true);
            refreshButton.setDisable(false);
            uploadFileButton.setDisable(false);
            downloadFileButton.setDisable(false);
            deleteButton.setDisable(false);

            bottomTextField.setText("Connected to server");
            connectMenuItem.setDisable(true);
        } else {
            bottomTextField.setText("Couldn't connect to server");
        }
    }

    @FXML
    private void close(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void refreshFilesList(ActionEvent actionEvent) {
        List<String> files = ftpOperations.getFiles();
        if (listView.getItems().isEmpty()) {
            listView.getItems().addAll(files);
        } else {
            listView.getItems().clear();
            listView.getItems().addAll(files);
        }
    }

    @FXML
    private void uploadFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(uploadFileButton.getScene().getWindow());
        if (file != null && ftpOperations.uploadFile(file)) {
            bottomTextField.setText("File uploaded successfully!");
            refreshFilesList(actionEvent);
        } else {
            bottomTextField.setText("Couldn't upload file | File already uploaded");
        }
    }

    @FXML
    private void downloadFile(ActionEvent actionEvent) {
        String selectedItemName = listView.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(selectedItemName);
        if (selectedItemName != null && !selectedItemName.contains("/")) {
            File file = fileChooser.showSaveDialog(downloadFileButton.getScene().getWindow());
            bottomTextField.setText("Downloading..");
            if (file != null && ftpOperations.getFile(selectedItemName, file)) {
                refreshFilesList(actionEvent);
                bottomTextField.setText("Downloaded!");
            } else if (file == null) {
                bottomTextField.setText("Destination folder not selected");
            } else {
                bottomTextField.setText("Couldn't download");
            }
        } else {
            bottomTextField.setText("You can't download a directory");
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        String selectedItemName = listView.getSelectionModel().getSelectedItem();
        if (ftpOperations.deleteFile(selectedItemName)) {
            bottomTextField.setText("File deleted!");
            refreshFilesList(actionEvent);
        } else {
            bottomTextField.setText("Couldn't delete");
        }
    }

    @FXML
    private void onListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            navigateTo();
        }
    }

    @FXML
    private void onListViewEnterPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            navigateTo();
        } else if(keyEvent.getCode() == KeyCode.LEFT) {
            downloadFile(null);
        } else if(keyEvent.getCode() == KeyCode.RIGHT) {
            uploadFile(null);
        }
    }

    private void navigateTo() {
        List<String> files;
        String directoryName = listView.getSelectionModel().getSelectedItem();

        if (directoryName.contains("/") || directoryName.equals("..")) {
            files = ftpOperations.goToDirectory(directoryName);
            if (listView.getItems().isEmpty()) {
                listView.getItems().addAll(files);
            } else {
                listView.getItems().clear();
                listView.getItems().addAll(files);
            }
        } else {
            bottomTextField.setText(directoryName + " is not a directory");
        }
    }
}
