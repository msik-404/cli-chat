package com.msik404.clichat.server;

import com.msik404.clichat.message.ConnectionLostException;
import com.msik404.clichat.message.MessageWriter;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

public record ClientOutputHandler(SocketChannel socketChannel, MessageWriter writer, Semaphore mutex) {

    public ClientOutputHandler(SocketChannel socketChannel) {
        this(socketChannel, new MessageWriter(socketChannel), new Semaphore(1));
    }

    private static String formatMessage(SocketAddress clientName, String message) {

        var builder = new StringBuilder();
        builder.append(clientName.toString());
        if (message.equals("STOP")) {
            builder.append(" has left.");
            return builder.toString();
        }
        builder.append(" has said: ");
        builder.append(message);
        return builder.toString();
    }

    public void sendMessage(
            SocketAddress clientName,
            String message
    ) throws IOException, InterruptedException, ConnectionLostException {

        String formattedMessage = formatMessage(clientName, message);
        mutex.acquire();
        writer.addMessage(formattedMessage);
        writer.writeAllToChannel();
        mutex.release();
    }
}
