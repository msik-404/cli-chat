package com.msik404.clichat.server;

import com.msik404.clichat.message.Header;
import com.msik404.clichat.message.MessageBufferHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

public record ClientOutputHandler(SocketChannel socketChannel, ByteBuffer buffer, Semaphore mutex) {

    public ClientOutputHandler(SocketChannel socketChannel) {
        this(socketChannel, ByteBuffer.allocate(Header.size() + MessageBufferHandler.MAX_BUFFER_SIZE), new Semaphore(1));
    }

    private static String formatMessage(SocketAddress clientName, String message) {
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

    public void sendMessage(SocketAddress clientName, Header header, String message) throws IOException, InterruptedException {
        String formattedMessage = formatMessage(clientName, message);
        mutex.acquire();
        int bufferedAmount = MessageBufferHandler.putMessage(buffer, header, formattedMessage);
        buffer.flip();
        while (bufferedAmount != 0) {
            bufferedAmount -= socketChannel.write(buffer);
        }
        buffer.clear();
        mutex.release();
        header.reset();
    }
}
