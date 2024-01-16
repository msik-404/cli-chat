package com.msik404.clichat.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final ConcurrentMap<SocketAddress, SocketWithOutput> outputs;

    public ClientHandler(Socket socket, ConcurrentMap<SocketAddress, SocketWithOutput> outputs) {

        this.socket = socket;
        this.outputs = outputs;
    }

    private String formatMessage(SocketAddress clientName, String message) {
        var builder = new StringBuilder();
        builder.append(clientName.toString());
        if (message == null) {
            builder.append(" has left.");
            return builder.toString();
        }
        builder.append(" has said: ");
        builder.append(message);
        return builder.toString();
    }

    @Override
    public void run() {

        try {
            var input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            String message;
            do {
                message = (String) input.readObject();
                for (SocketWithOutput socketWithOutput : outputs.values()) {
                    var otherSocket = socketWithOutput.socket();
                    if (!socket.equals(otherSocket)) {
                        var output = socketWithOutput.output();
                        try {
                            output.writeObject(formatMessage(socket.getRemoteSocketAddress(), message));
                        } catch (IOException ex)  {
                            outputs.remove(otherSocket.getRemoteSocketAddress());
                            LOGGER.log(Level.WARNING, "Write to client has failed. Probably client's socket has been closed.");
                        }
                    }
                }
            } while (message != null); // When client wishes to exit, he will send null object.

        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Read from client has failed. Probably client has left unexpectedly.");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Wrong class has ben send.");
            throw new RuntimeException(ex.getMessage());
        } finally {
            try {
                // When client wishes to exit or some error happened, remove client from message receivers.
                outputs.remove(socket.getRemoteSocketAddress());
                if (!socket.isClosed()) {
                    socket.close();
                }
                LOGGER.log(Level.INFO, "User: " + socket.getRemoteSocketAddress() + " has disconnected.");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Socket could not be closed for some reason.");
                throw new RuntimeException(ex.getMessage());
            }
        }
    }
}
