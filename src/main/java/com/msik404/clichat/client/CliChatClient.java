package com.msik404.clichat.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliChatClient implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(CliChatClient.class.getName());

    private final Socket server;

    public CliChatClient(InetAddress address, int port) throws IOException {

        this.server = new Socket(address, port);
    }

    public void run() throws IOException {

        var output = new ObjectOutputStream(server.getOutputStream());
        var input = new ObjectInputStream(new BufferedInputStream(server.getInputStream()));

        Thread listener = new Thread(new ClientInputHandler(input));
        listener.start();

        var scanner = new Scanner(System.in);
        while (true) {
            var message = scanner.nextLine();
            if (message.equals("STOP")) {
                output.writeObject(null);
                listener.interrupt(); // Stop listening.
                LOGGER.log(Level.INFO, "Client disconnects.");
                break;
            }
            output.writeObject(message);
        }
    }

    @Override
    public void close() throws IOException {
        server.close();
    }
}
