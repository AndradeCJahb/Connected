package connections;

import org.glassfish.tyrus.server.Server;

public class ConnectionsBackend {
    public static void main(String[] args) {
        String port = System.getenv("PORT");
        int serverPort = (port != null) ? Integer.parseInt(port) : 8080; // Default to 8080 if PORT is not set
        Server server = new Server("0.0.0.0", serverPort, "/", null, WebSocketServer.class);
        System.out.println("Starting server on port " + serverPort);
        try {
            server.start();          

            Thread.currentThread().join(); 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}