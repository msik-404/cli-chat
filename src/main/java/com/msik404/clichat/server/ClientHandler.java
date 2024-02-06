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
    private final ConcurrentMap<SocketAddress, ClientOutputHandler> outputs;

    public ClientHandler(Socket socket, ConcurrentMap<SocketAddress, ClientOutputHandler> outputs) {

        this.socket = socket;
        this.outputs = outputs;
    }

    @Override
    public void run() {
        try (var input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()))) {
            String message;
            do {
                message = (String) input.readObject();
                for (ClientOutputHandler clientOutputHandler : outputs.values()) {
                    var otherSocket = clientOutputHandler.socket();
                    if (!socket.equals(otherSocket)) {
                        try {
                            clientOutputHandler.sendMessage(socket.getRemoteSocketAddress(), message);
                        } catch (IOException ex) {
                            outputs.remove(otherSocket.getRemoteSocketAddress());
                            LOGGER.log(
                                    Level.WARNING,
                                    "Write to client has failed. Probably client's socket has been closed."
                            );
                        } catch (InterruptedException ex) {
                            LOGGER.log(
                                    Level.SEVERE,
                                    "Current task has been interrupted from outside while waiting for mutex to " +
                                            "send message to client."
                            );
                            message = null; // signal to get to final block.
                            break;
                        }
                    }
                }
            } while (message != null); // When client wishes to exit, he will send null object.

        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Read from client has failed. Probably client has left unexpectedly.");
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Unexpected class has ben received.");
        } finally {
            try {
                // When client wishes to exit or some error happened, remove client from message receivers.
                // Only leaving clients close sockets.
                outputs.remove(socket.getRemoteSocketAddress());
                if (!socket.isClosed()) {
                    socket.close();
                }
                LOGGER.log(Level.INFO, "User: " + socket.getRemoteSocketAddress() + " has disconnected.");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Socket threw exception during attempt of closing it.");
            }
        }
    }
}
