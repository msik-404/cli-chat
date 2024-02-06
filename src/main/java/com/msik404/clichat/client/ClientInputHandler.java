package com.msik404.clichat.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public record ClientInputHandler(ObjectInputStream input) implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientInputHandler.class.getName());

    @Override
    public void run() {
        while (true) {
            try {
                if (Thread.interrupted()) {
                    LOGGER.log(Level.INFO, "Stopping listening due to client disconnecting.");
                    return;
                }
                var message = (String) input.readObject();
                System.out.println(message);
            } catch (IOException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Could not read from the server. Probably server's or client's socket has been closed."
                );
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.WARNING, "Unexpected class has ben received.");
            }
        }
    }
}
