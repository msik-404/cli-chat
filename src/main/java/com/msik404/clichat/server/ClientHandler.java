package com.msik404.clichat.server;

import com.msik404.clichat.message.ConnectionLostException;
import com.msik404.clichat.message.MessageReader;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final SocketChannel socketChannel;
    private final ConcurrentMap<SocketAddress, ClientOutputHandler> outputs;
    private final MessageReader reader;

    public ClientHandler(SocketChannel socketChannel, ConcurrentMap<SocketAddress, ClientOutputHandler> outputs) {

        this.socketChannel = socketChannel;
        this.outputs = outputs;
        this.reader = new MessageReader(socketChannel);
    }

    @Override
    public void run() {
        boolean sending = true;
        try {
            while (sending) {
                reader.readFromChannel();
                for (ClientOutputHandler clientOutputHandler : outputs.values()) {
                    var otherChannel = clientOutputHandler.socketChannel();
                    if (!socketChannel.equals(otherChannel)) {
                        for (String message : reader.getMessageBuffer()) {
                            if (message.equals("STOP")) {
                                sending = false;
                            }
                            try {
                                clientOutputHandler.sendMessage(socketChannel.getRemoteAddress(), message);
                            } catch (IOException | ConnectionLostException ex) {
                                outputs.remove(otherChannel.getRemoteAddress());
                                LOGGER.log(
                                        Level.WARNING,
                                        "Write to client has failed. Probably client's socket has been closed."
                                );
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Read from client has failed. Probably client has left unexpectedly.");
        } catch (InterruptedException ex) {
            LOGGER.log(
                    Level.SEVERE,
                    "Current task has been interrupted from outside while waiting for mutex to " +
                            "send message to client."
            );
        } finally {
            try {
                // When client wishes to exit or some error happened, remove client from message receivers.
                // Only leaving clients close sockets.
                outputs.remove(socketChannel.getRemoteAddress());
                LOGGER.log(Level.INFO, "User: " + socketChannel.getRemoteAddress() + " has disconnected.");
                socketChannel.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Socket threw exception during attempt of closing it.");
            }
        }
    }
}
