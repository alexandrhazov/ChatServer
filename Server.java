package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (Connection con : connectionMap.values()) {
            try {
                con.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Invalid try" + e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Введите порт");
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен.");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        serverSocket.close();
    }
    
    private static class Handler extends Thread {
        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String result;
            while (true) {
               connection.send(new Message(MessageType.NAME_REQUEST));
               Message receiver = connection.receive();

               if (receiver.getType() == MessageType.USER_NAME &&
                        receiver.getData() != null && !Objects.equals(receiver.getData(), "")) {
                   if (!connectionMap.containsKey(receiver.getData())) {
                        connectionMap.put(receiver.getData(), connection);
                       connection.send(new Message(MessageType.NAME_ACCEPTED));
                       ConsoleHelper.writeMessage(receiver.getData() + " принято!");
                       result = receiver.getData();
                       break;
                   }
               }
           }
            return result;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                String name = entry.getKey();
                if (!name.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message receiver = connection.receive();
                if (receiver.getType() == MessageType.TEXT) {
                    Message message = new Message(MessageType.TEXT, userName + ": " + receiver.getData());
                    sendBroadcastMessage(message);
                }
                else {
                    ConsoleHelper.writeMessage("Not type of text");
                }
            }
        }

        @Override
        public void run() {
            String newUser = null;
            ConsoleHelper.writeMessage("New connection has been established from: " + socket.getRemoteSocketAddress());
            try(Connection connection = new Connection(socket)) {
                newUser = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, newUser));
                notifyUsers(connection, newUser);
                serverMainLoop(connection, newUser);
            }
            catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error occurred");
            }
            if (newUser != null) {
                connectionMap.remove(newUser);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, newUser));
            }
        }
    }
}
