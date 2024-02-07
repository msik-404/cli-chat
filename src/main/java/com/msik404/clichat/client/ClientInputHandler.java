package com.msik404.clichat.client;

import com.msik404.clichat.message.Header;
import com.msik404.clichat.message.MessageBufferHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public record ClientInputHandler(SocketChannel serverChannel) implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientInputHandler.class.getName());

    @Override
    public void run() {

        var buffer = ByteBuffer.allocate(Header.size() + MessageBufferHandler.MAX_BUFFER_SIZE);
        int bufferedAmount = 0;
        boolean writeMode = true;

        var header = new Header();

        try {
            while (true) {
                int count = serverChannel.read(buffer);
                if (count == -1) {
                    LOGGER.log(Level.WARNING, "Connection with the server has been lost.");
                    return;
                }
                if (Thread.interrupted()) {
                    LOGGER.log(Level.INFO, "Stopping listening due to client disconnecting.");
                    return;
                }
                bufferedAmount += count;
                if (header.isEmpty() && bufferedAmount >= Header.size()) {
                    buffer.flip();
                    writeMode = false;
                    bufferedAmount -= header.getFrom(buffer);
                }
                if (!header.isEmpty() && bufferedAmount >= header.getMessageLength()) {
                    if (writeMode) {
                        buffer.flip();
                    }
                    System.out.println(MessageBufferHandler.getMessage(buffer, header));
                    bufferedAmount -= header.getMessageLength();
                    header.clear();
                }
                if (buffer.hasRemaining()) {
                    buffer.compact();
                } else {
                    buffer.clear();
                }
                writeMode = true;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read from the server. Probably server's or client's socket has been closed.");
        }
    }
}
