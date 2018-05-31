package server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import server.model.ServerImplementation;
import server.model.ServerInterface;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Controller {
    @FXML
    public MenuItem startServerMenuItem;
    @FXML
    private TextArea serverLogArea;
    private Registry registry;
    private String hostname;

    @FXML
    private void startServer(ActionEvent actionEvent) {
        serverLogArea.appendText("FTP Server starting\n");

        String codebase = new File("").getAbsolutePath() + "/src/server";
        //System.setProperty("java.rmi.server.codebase", codebase);
        System.setProperty("java.security.policy", codebase + "/model/server.policy");
        System.setSecurityManager(new SecurityManager());
        try {
            ServerInterface server = new ServerImplementation(this);
            hostname = InetAddress.getLocalHost().getHostAddress();
            registry = LocateRegistry.createRegistry(4099);
            registry.rebind("rmi://" + hostname + ":4099/FTP", server);
            log("Server started");
            startServerMenuItem.setDisable(true);
        } catch (RemoteException | UnknownHostException e) {
            log("Exception: " + e);
        }
    }

    @FXML
    private void closeServer(ActionEvent actionEvent) {
        if (registry != null) {
            try {
                UnicastRemoteObject.unexportObject(registry, true);
                registry.unbind("rmi://" + hostname + ":4099/FTP");
            } catch (RemoteException | NotBoundException e) {
                serverLogArea.appendText("Exception: " + e + "\n");
            }
        }
        Platform.exit();
        System.exit(0);
    }

    public void log(String msg) {
        serverLogArea.appendText(msg + "\n");
    }
}
