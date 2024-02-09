package com.msik404.clichat.client;

import com.msik404.clichat.message.ConnectionLostException;
import com.msik404.clichat.message.MessageReader;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public record ClientInputHandler(MessageReader reader) implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientInputHandler.class.getName());

    public ClientInputHandler(SocketChannel serverChannel) {
        this(new MessageReader(serverChannel));
    }

    @Override
    public void run() {
        try {
            while (true) {
                reader.readFromChannel();
                if (Thread.interrupted()) {
                    LOGGER.log(Level.INFO, "Stopping listening due to client disconnecting.");
                    return;
                }
                var messages = reader.getMessageBuffer();
                for (String message : messages) {
                    System.out.println(message);
                }
                messages.clear();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not read from the server. Probably server's or client's socket has been closed.");
        } catch (ConnectionLostException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            System.exit(1);
        }
    }
}
