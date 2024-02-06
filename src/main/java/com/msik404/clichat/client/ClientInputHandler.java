package com.msik404.clichat.client;

import com.msik404.clichat.message.Header;
import com.msik404.clichat.message.MessageBuffer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public record ClientInputHandler(SocketChannel serverChannel) implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientInputHandler.class.getName());

    @Override
    public void run() {

        var buffer = new MessageBuffer();
        Header header = null;

        try {
            var selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_READ);

            while (true) {
                if (Thread.interrupted()) {
                    LOGGER.log(Level.INFO, "Stopping listening due to client disconnecting.");
                    return;
                }
                selector.select();
                buffer.readFromChannel(serverChannel);
                if (header == null) {
                    Optional<Header> optional = buffer.readHeader();
                    if (optional.isPresent()) {
                        header = optional.get();
                    }
                }
                if (header != null) {
                    Optional<String> optional = buffer.readMessage(header);
                    if (optional.isPresent()) {
                        header = null;
                        System.out.println(optional.get());
                        buffer.clear();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "Could not read from the server. Probably server's or client's socket has been closed."
            );
        }
    }
}
