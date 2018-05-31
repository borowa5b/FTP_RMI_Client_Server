package server.model;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {
    String open() throws RemoteException;

    List<String> ls(String path) throws RemoteException;

    List<String> cd(String directoryName, String currentPath) throws RemoteException;

    void putFile(byte[] fileData, String path) throws IOException;

    boolean deleteFile(String filePath) throws IOException;

    byte[] getFile(String filePath, int offset, int fileSize) throws RemoteException;

    int getFileSize(String filePath) throws RemoteException;
}
