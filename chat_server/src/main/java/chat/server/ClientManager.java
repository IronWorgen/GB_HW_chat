package chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientManager implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    public static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {

        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату");
            broadcastMessage("Server: " + name + " подключился к чату");

        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);

        }

    }


    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат");
        broadcastMessage(name + " покинул чат");

    }


    @Override
    public void run() {

        String massageFromClient;
        while (socket.isConnected()) {
            try {
                massageFromClient = bufferedReader.readLine();
                if (massageFromClient == null) {
                    // для macOS
                    closeEverything(socket, bufferedWriter, bufferedReader);
                    break;
                }

                if (massageFromClient.contains("@")) {
                    Pattern pattern = Pattern.compile("@[^:]+:");
                    Matcher matcher = pattern.matcher(massageFromClient);
                    String recipientName = null;
                    String messageToRecipient = "";

                    while (matcher.find()) {
                        recipientName = massageFromClient.substring(matcher.start() + 1, matcher.end() - 1).trim();
                        try {
                            messageToRecipient = massageFromClient.substring(matcher.end() + 1).trim();
                        } catch (StringIndexOutOfBoundsException e) {
                            System.out.println("Клиент " + name + " прислал пустое сообщение");
                        }
                    }
                    if (recipientName == null) {
                        sendMessage("@Server: неверный формат имени. " +
                                "Для отправки личного сообщения введите: @Имя получателя: текст сообщения", name);
                    } else if (messageToRecipient.length() != 0) {
                        sendMessage("@" + name + ": " + messageToRecipient, recipientName);
                    }
                } else {
                    broadcastMessage(massageFromClient);
                }

            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
                break;
            }
        }

    }

    /**
     * отправить приватное сообщение
     *
     * @param message - текст сообщения
     * @param name    - имя пользователя
     */
    private void sendMessage(String message, String name) {
        for (ClientManager client : clients) {
            if (client.name.equals(name)) {
                try {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                    return;
                } catch (IOException e) {
                    closeEverything(socket, bufferedWriter, bufferedReader);
                    return;
                }
            }
        }
        try {
            this.bufferedWriter.write("@Server: не найден пользователь с именем " + name);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();

        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }

    /**
     * отправка сообщения всем пользователям
     *
     * @param message - текст сообщения
     */
    private void broadcastMessage(String message) {
        for (ClientManager client : clients) {
            try {
                if (!client.name.equals(name) && message != null) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    private void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClient();
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
