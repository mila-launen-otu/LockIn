package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class WhiteboardServer {
    private static final int PORT = 5002;
    private static final String SERVER_IP = "0.0.0.0";
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private File csvFile;

    public WhiteboardServer() {
        // Create a CSV file to store coordinates
        csvFile = new File("whiteboard_data.csv");

        //If the CSV doesn't exist, create it.
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
                // Initialize with header
                FileWriter fw = new FileWriter(csvFile);
                fw.write("x,y,type,color,lineWidth\n");
                fw.close();
            } catch (IOException e) {
                System.err.println("Error creating CSV file: " + e.getMessage());
            }
        }
    }

    public void start() {

        // Start server
        try (ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(SERVER_IP))) {
            System.out.println("Whiteboard Server started on " + SERVER_IP + ":" + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create handler for this client
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);

                // Start client handler thread
                new Thread(handler).start();

                // Send current CSV data to new client
                sendCurrentCsvData(handler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public void broadcastUpdate(String coordinates, ClientHandler sender) {
        // Add to CSV file
        try (FileWriter fw = new FileWriter(csvFile, true)) {
            fw.write(coordinates + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }

        // Send to all other clients
        for (ClientHandler client : clients) {
            client.sendCoordinates(coordinates);
        }
    }

    private void sendCurrentCsvData(ClientHandler newClient) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                newClient.sendCoordinates(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected");
    }

    public void clearCanvas(ClientHandler sender) {
        // Reset the CSV file with just the header
        try {
            FileWriter fw = new FileWriter(csvFile);
            fw.write("x,y,type,color,lineWidth\n");
            fw.close();
            System.out.println("Canvas cleared.");
        } catch (IOException e) {
            System.err.println("Error clearing CSV file: " + e.getMessage());
        }

        // Notify all clients to clear their canvases
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendCoordinates("CLEAR");
            }
        }
    }

    // Inner class to handle client connections
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final WhiteboardServer server;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, WhiteboardServer server) {
            this.clientSocket = socket;
            this.server = server;

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Error setting up client streams: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if("CLEAR".equals(inputLine)){
                        server.clearCanvas(this);
                    }else{
                        // Process received coordinates and broadcast
                        server.broadcastUpdate(inputLine, this);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
                server.removeClient(this);
            }
        }

        public void sendCoordinates(String coordinates) {
            out.println(coordinates);
        }
    }

    public static void main(String[] args) {
        WhiteboardServer server = new WhiteboardServer();
        server.start();
    }
}