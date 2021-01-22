package com.chat.client;

import com.chat.Connection;
import com.chat.ConsoleHelper;
import com.chat.Message;
import com.chat.MessageType;
import java.io.IOException;
import java.net.Socket;

/**
 * @author Pavel Zeger
 */
public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("The %s was added to the chat", userName));
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(String.format("The %s has left the chat", userName));
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                    continue;
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else {
                    throw new IOException("Unexpected MessageType");

                }

            }

        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                    continue;

                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                    continue;

                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                    continue;

                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }

        }

        @Override
        public void run() {
            String serverAddress = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(serverAddress, port);
                Client.this.connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                notifyConnectionStatusChanged(false);
            }

        }

    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter username:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));

        } catch (IOException e) {
            e.printStackTrace();
            ConsoleHelper.writeMessage(e.getMessage());
            clientConnected = false;

        }

    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this) {
            try {
                this.wait();
            } catch (Exception e) {
                ConsoleHelper.writeMessage("Error!");
            }
        }

        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equals("exit")) {
                break;
            }
            if (Boolean.TRUE.equals(shouldSendTextFromConsole())) {
                sendTextMessage(text);
            }
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

}
