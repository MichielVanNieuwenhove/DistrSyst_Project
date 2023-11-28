package Client;


import Interface.BulletinBoard;

import java.rmi.RemoteException;

public class ReadThread extends Thread {
    BulletinBoard board;
    Connection connection;
    Main_Client client;

    public ReadThread(BulletinBoard board, Connection connection, Main_Client client) {
        this.board = board;
        this.connection = connection;
        this.client = client;
    }

    public void run() {
        while(true){
            try {
                String encryptedData = board.get(connection.getIndex_other(), connection.getTag_other());
                client.receive(encryptedData, connection);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
