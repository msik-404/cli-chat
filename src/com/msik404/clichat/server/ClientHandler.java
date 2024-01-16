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

    @Override
    public void run() {

        try {
            var input = new ObjectInputStream(socket.getInputStream());

            String message;
            do {
                message = (String) input.readObject();
                for (SocketWithOutput socketWithOutput : outputs.values()) {
                    var otherSocket = socketWithOutput.socket();
                    if (!socket.equals(otherSocket)) {
                        var output = socketWithOutput.output();
                        try {
                            output.writeObject(socket.getRemoteSocketAddress() + " said: " + message);
                        } catch (IOException ex)  {
                            outputs.remove(otherSocket.getRemoteSocketAddress());
                            LOGGER.log(Level.WARNING, "Write to client has failed. Probably client's socket has been closed.");
                        }
                    }
                }
            } while (message != null); // When client wishes to exit, he will send null object.

            // When client wishes to exit or some error happened, remove client from message receivers.
            socket.close();
            outputs.remove(socket.getRemoteSocketAddress());
        } catch (IOException ex) {
            outputs.remove(socket.getRemoteSocketAddress());
            LOGGER.log(Level.WARNING, "Read from client has failed. Probably client has left unexpectedly.");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
