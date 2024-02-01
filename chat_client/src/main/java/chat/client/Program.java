package chat.client;

import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите ваше имя:\t");
            String name = scanner.nextLine().trim();

            InetAddress address = InetAddress.getLocalHost();
            Socket socket = new Socket(address, 5000);

            Client client = new Client(socket, name);
            InetAddress inetAddress = InetAddress.getLocalHost();

            System.out.println("InetAddress: " + inetAddress);
            String removeIP = inetAddress.getHostAddress();
            System.out.println("Remove IP: " + removeIP);
            System.out.println("LocalPort: " + socket.getLocalPort());

            System.out.println("-----------------------------------------");
            System.out.println("System: Для отправки личного сообщения введите @имя получателя: тест сообщения");
            System.out.println("-----------------------------------------");

            client.listenForMessage();
            client.sandMessage();

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
