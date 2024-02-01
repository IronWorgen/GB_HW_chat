package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket socket) {
        this.serverSocket = socket;
    }

    public void runServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("подключен новый клиент!");
                ClientManager clientManager = new ClientManager(socket);
                Thread thread = new Thread(clientManager);
                thread.start();
            }
        } catch (IOException e) {
            closeSocket(serverSocket);
        }

    }

    private void closeSocket(ServerSocket socket) {

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
