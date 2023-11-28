package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote {
    void write(int idx, String u, String tag) throws RemoteException;

    String get(int idx, String tag) throws RemoteException;
}
