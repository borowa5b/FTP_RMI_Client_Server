package client.model;

import server.model.ServerInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.List;

public class FTPOperations {
    private ServerInterface serverInterface;
    private String serverPath;

    public boolean connect() {
        boolean connected = true;
        try {
            String hostname = InetAddress.getLocalHost().getHostAddress();
            Registry registry = LocateRegistry.getRegistry(4099);
            serverInterface = (ServerInterface) registry.lookup("rmi://" + hostname + ":4099/FTP");
            serverPath = serverInterface.open();
            if (serverPath.equals("0")) {
                connected = false;
            }
        } catch (UnknownHostException | RemoteException | NotBoundException e) {
            e.printStackTrace();
            connected = false;
        }
        return connected;
    }

    public List<String> getFiles() {
        List<String> filesList = Collections.emptyList();
        try {
            filesList = serverInterface.ls(serverPath);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return filesList;
    }

    public boolean uploadFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] fileData = new byte[(int) file.length()];
            while (fileInputStream.read(fileData) != -1) {
                serverInterface.putFile(fileData, serverPath + "/" + file.getName());
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean getFile(String fileName, File file) {
        //convert array of bytes into file
        int chunkSize = 10 * 1024 * 1024;
        int fileSize;
        try {
            fileSize = serverInterface.getFileSize(serverPath + "/" + fileName);
        } catch (RemoteException e) {
            return false;
        }
        int offset = 0;
        while (fileSize > chunkSize) {
            byte[] buffer;
            try {
                buffer = serverInterface.getFile(serverPath + "/" + fileName, offset, chunkSize);
            } catch (RemoteException e) {
                return false;
            }
            try (FileOutputStream fileOutput = new FileOutputStream(file, true)) {
                fileOutput.write(buffer, 0, chunkSize);
            } catch (IOException e) {
                return false;
            }
            fileSize -= chunkSize;
        }

        if (fileSize != 0) {
            byte[] buffer;
            try {
                buffer = serverInterface.getFile(serverPath + "/" + fileName, offset, fileSize);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
            try (FileOutputStream fileOutput = new FileOutputStream(file, true)) {
                fileOutput.write(buffer, 0, fileSize);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } //convert array of bytes into file

        return true;
    }

    public boolean deleteFile(String fileName) {
        try {
            return serverInterface.deleteFile(serverPath + "/" + fileName);
        } catch (IOException e) {
            return false;
        }
    }

    public List<String> goToDirectory(String directoryName) {
        List<String> filesList = Collections.emptyList();
        try {
            if (directoryName.equals("..")) {
                filesList = serverInterface.cd("..", serverPath);
                String[] fields = serverPath.split("/");
                StringBuilder pathBuilder = new StringBuilder();
                for (int field = 0; field < fields.length - 1; field++) {
                    if (!fields[field].equals(""))
                        pathBuilder.append("/").append(fields[field]);
                }
                serverPath = pathBuilder.toString();
            } else {
                directoryName = directoryName.substring(0, directoryName.length() - 1);
                filesList = serverInterface.cd(directoryName, serverPath);
                serverPath += "/" + directoryName;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return filesList;
    }
}
