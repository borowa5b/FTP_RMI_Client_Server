package server.model;

import server.Controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ServerImplementation extends UnicastRemoteObject implements ServerInterface {
    private Controller controller;
    private String basePath;

    public ServerImplementation(Controller controller) throws RemoteException {
        super();
        this.controller = controller;
    }

    @Override
    public String open() throws RemoteException {
        try {
            controller.log("Connection established from ip:" + RemoteServer.getClientHost());
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        String path = "0";
        try {
            path = new java.io.File(".").getCanonicalPath() + "/FTP";
            basePath = path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    @Override
    public List<String> ls(String path) throws RemoteException {
        controller.log("ls: " + path);

        List<String> output = new ArrayList<>();
        File folder = new File(path);

        if (!basePath.equals(path)) {
            output.add("..");
        }

        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    output.add(file.getName() + "/");
                } else {
                    output.add(file.getName());
                }
            }
        } else {
            output.add("No files!");
            return output;
        }
        return output;
    }

    @Override
    public List<String> cd(String directoryName, String currentPath) throws RemoteException {
        controller.log("cd: " + currentPath + "/" + directoryName);

        if (directoryName.equals("..")) {
            String[] fields = currentPath.split("/");
            StringBuilder pathBuilder = new StringBuilder();
            for (int field = 0; field < fields.length - 1; field++) {
                if (!fields[field].equals(""))
                    pathBuilder.append("/").append(fields[field]);
            }
            return ls(pathBuilder.toString());
        } else {
            return ls(currentPath + "/" + directoryName);
        }
    }

    @Override
    public void putFile(byte[] fileData, String path) throws IOException {
        controller.log("putFile: " + path);

        Path file = Paths.get(path);
        Files.createFile(file);
        Files.write(file, fileData, StandardOpenOption.APPEND);
    }

    @Override
    public boolean deleteFile(String filePath) throws IOException {
        controller.log("deleteFile: " + filePath);

        return Files.deleteIfExists(Paths.get(filePath));
    }

    @Override
    public byte[] getFile(String filePath, int offset, int fileSize) throws RemoteException {
        controller.log("getFile: " + filePath);

        try {
            File file = new File(filePath);
            byte buffer[] = new byte[(int) file.length()];
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(filePath));
            int n = input.read(buffer, offset, fileSize);
            input.close();
            if (n == -1) return null;
            return (buffer);
        } catch (Exception e) {
            controller.log("FileImplementation: " + e.getMessage());
            e.printStackTrace();
            return (null);
        }
    }

    @Override
    public int getFileSize(String filePath) throws RemoteException {
        controller.log("getFileSize: " + filePath);

        File file = new File(filePath);
        return (int) file.length();
    }
}
