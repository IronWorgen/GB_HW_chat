package chat.client;

import lombok.RequiredArgsConstructor;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Client {
    private final Socket socket;
    private final String name;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
            throw new RuntimeException(e);
        }

    }

    /**
     * слушатель входящих сообщений
     */
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                while (socket.isConnected()) {
                    try {
                        message = bufferedReader.readLine();
                        if (message.charAt(0) == '@') {
                            System.out.println("\u001B[31m" + message + "\u001B[0m");
                        } else {
                            System.out.println(message);
                        }

                    } catch (IOException e) {
                        closeEverything(socket, bufferedWriter, bufferedReader);
                        throw new RuntimeException(e);
                    }

                }
            }
        }).start();
    }

    /**
     * отправка сообщения
     */
    public void sandMessage() {
        try {
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                if (message.length() != 0) {
                    bufferedWriter.write(name + ": " + message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * закрыть все
     *
     * @param socket
     * @param bufferedWriter
     * @param bufferedReader
     */
    private void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {

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
