package com.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pavel Zeger
 */
public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = ConsoleHelper.readInt();
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("The local server is up");

            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class Handler extends Thread {
        final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage(String.format("New connection created with %s", socket.getRemoteSocketAddress()));
            Connection connection = null;
            String userName = null;
            try {
                connection = new Connection(socket);
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Error occurred during data receiving with a remote host!");
            }

            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

            ConsoleHelper.writeMessage("The connection with a remote server closed!");

        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message message = null;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST, "Enter name:"));
                message = connection.receive();
                String userName = message.getData();
                if (message.getType() == MessageType.USER_NAME
                        && !userName.isEmpty()
                        && !connectionMap.containsKey(userName)) {
                    connectionMap.put(userName, connection);
                    break;
                }

            }
            connection.send(new Message(MessageType.NAME_ACCEPTED, "The new username was accepted!"));
            ConsoleHelper.writeMessage("The new username was accepted!");
            return message.getData();
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                String clientName = entry.getKey();
                Message message = new Message(MessageType.USER_ADDED, clientName);
                if (!clientName.equals(userName)) {
                    connection.send(message);
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message =  connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, String.format("%s: %s", userName, message.getData())));
                } else {
                    ConsoleHelper.writeMessage("This is not text message!");
                }
            }

        }

    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("The message cannot be send!");
            }
        }
    }

}
