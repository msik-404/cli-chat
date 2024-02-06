package com.msik404.clichat.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Semaphore;

public record ClientOutputHandler(Socket socket, ObjectOutputStream output, Semaphore mutex) {

    public ClientOutputHandler(Socket socket) throws IOException {
        this(socket, new ObjectOutputStream(socket.getOutputStream()), new Semaphore(1));
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

    public void sendMessage(SocketAddress clientName, String message) throws IOException, InterruptedException {
        mutex.acquire();
        output.writeObject(formatMessage(clientName, message));
        mutex.release();
    }
}
