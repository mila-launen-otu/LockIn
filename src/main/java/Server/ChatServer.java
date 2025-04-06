package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final Map<String, ClientHandler> clients = new HashMap<>();
    private static final int PORT = 5001;
    private static final String SERVER_IP = "0.0.0.0"; // Listen on all interfaces

    public static void main(String[] args) throws IOException {

        System.out.println("Chat server started at " + SERVER_IP + ": " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_IP));

        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        ChatServer server;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Prompt client for username
                username = in.readLine();
               //TODO: add to clients
                synchronized (clients) {
                    clients.put(username, this);
                }

                // Send list of connected clients and their names
                StringBuilder clientsList = new StringBuilder("Connected users");
                // TODO: send list to client
                synchronized (clients) {
                    clientsList.append("(").append(clients.size()).append("): ");
                    for (String name : clients.keySet()) {
                        clientsList.append(name).append(" ");
                    }
                }

                out.println(clientsList.toString());

                // Notify other clients about new user
                synchronized (clientHandlers) {
                    for (ClientHandler handler : clientHandlers) {
                        handler.out.println(username + " has joined the chat!");
                    }
                }

                synchronized (clientHandlers) {
                    clientHandlers.add(this);
                }

                String message;

                while ((message = in.readLine()) != null) {
                    if(message.contains("ANS-")){
                        System.out.println(message);
                    }
                    else if(message.contains("NEWQ")){
                        System.out.println(message);
                    }
                    else{
                        System.out.println(username + ": " + message);
                    }
                   // TODO: broadcast message
                    synchronized (clientHandlers) {
                        for (ClientHandler handler : clientHandlers) {
                            if(message.contains("ANS-")){
                                handler.out.println(message);
                            }
                            if(message.contains("NEWQ")){
                                handler.out.println(message);
                            }
                            else{
                                handler.out.println(username + ": " + message);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientHandlers) {
                    clientHandlers.remove(this);
                }
                synchronized (clients) {
                    clients.remove(username);
                }

                // Notify others when a client leaves
                synchronized (clientHandlers) {
                    for (ClientHandler handler : clientHandlers) {
                        handler.out.println(username + " has left the chat.");
                    }
                }
            }
        }
    }


}

