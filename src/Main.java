import Client.Connection;
import Client.Main_Client;
import Server.Main_Server;

public class Main {
    public static void main(String[] args) throws Exception {
        Main_Server.main(null);

        Main_Client client0 = new Main_Client("Bert");
        Main_Client client1 = new Main_Client("Bob");
        Main_Client client2 = new Main_Client("An");
        //Main_Client client3 = new Main_Client("Marie");
        //Main_Client client4 = new Main_Client("Henk");

        connect(client0, client1);
        connect(client0, client2);
        connect(client1, client2);

    }

    public static void connect(Main_Client c0, Main_Client c1) throws Exception {
        Connection connection0 = c0.setup(c1.getName());
        Connection connection1 = c1.setup(c0.getName());

        c0.setConnection(connection1);
        c1.setConnection(connection0);
    }
}
