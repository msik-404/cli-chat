package com.msik404.clichat.client;

import com.msik404.clichat.message.Header;
import com.msik404.clichat.message.MessageBufferHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public record ClientInputHandler(SocketChannel serverChannel) implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientInputHandler.class.getName());

    @Override
    public void run() {

        var buffer = ByteBuffer.allocate(Header.size() + MessageBufferHandler.MAX_BUFFER_SIZE);
        int bufferedAmount = 0;
        boolean flipped = false;

        var header = new Header();

        try {
            while (true) {
                bufferedAmount += serverChannel.read(buffer);
                if (Thread.interrupted()) {
                    LOGGER.log(Level.INFO, "Stopping listening due to client disconnecting.");
                    return;
                }
                if (bufferedAmount >= Header.size()) {
                    buffer.flip();
                    flipped = true;
                    bufferedAmount -= header.updateFromBuffer(buffer);
                }
                if (header.isJustUpdated() && bufferedAmount >= header.getMessageLength()) {
                    if (!flipped) {
                        buffer.flip();
                    }
                    flipped = false;
                    System.out.println(MessageBufferHandler.getMessage(buffer, header));
                    bufferedAmount -= header.getMessageLength();
                    header.reset();
                    if (bufferedAmount == 0) {
                        buffer.clear();
                    } else {
                        buffer.compact();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read from the server. Probably server's or client's socket has been closed.");
        }
    }
}
