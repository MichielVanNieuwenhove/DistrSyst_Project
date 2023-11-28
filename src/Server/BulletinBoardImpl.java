package Server;

import Interface.BulletinBoard;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard {

    //array of sets -> make class set
    Map<String, String>[] board = new HashMap[16];

    public BulletinBoardImpl() throws RemoteException{}

    @Override
    public synchronized void write(int idx, String u, String tag) throws RemoteException {
        if (board[idx] == null) {
            board[idx] = new HashMap<>();
        }
        board[idx].put(tag, u);
        notifyAll();
    }

    @Override
    public synchronized String get(int idx, String tag) throws RemoteException {
        while (board[idx] == null || board[idx].get(tag) == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        String message = board[idx].get(tag);
        board[idx].remove(tag);
        return message;
    }
}
