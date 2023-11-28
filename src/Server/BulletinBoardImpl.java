package Server;

import Interface.BulletinBoard;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard {

    //array of sets -> make class set
    Map<String, String>[] board = new HashMap[16];

    public BulletinBoardImpl() throws RemoteException{}

    @Override
    public synchronized void write(int idx, String u, String tag) throws RemoteException {
        System.out.println("write: " + "\nidx: " + idx + "\nu: " + u + "\ntag: " + tag);
        if (board[idx] == null) {
            board[idx] = new HashMap<>();
        }
        board[idx].put(tag, u);
        notifyAll();
    }

    @Override
    public synchronized String get(int idx, String tag) throws RemoteException {
        System.out.println("get: " + "\nidx: " + idx + "\ntag: " + tag);
        while (board[idx] == null || board[idx].get(tag) == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return board[idx].get(tag);
    }
}
