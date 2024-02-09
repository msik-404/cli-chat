package com.msik404.clichat.client;

import com.msik404.clichat.message.ConnectionLostException;
import com.msik404.clichat.message.MessageWriter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliChatClient {

    private static final Logger LOGGER = Logger.getLogger(CliChatClient.class.getName());

    private final InetSocketAddress serverAddress;

    public CliChatClient(InetSocketAddress socketAddress) {

        this.serverAddress = socketAddress;
    }

    public void run() throws IOException, ConnectionLostException {

        try (var serverChannel = SocketChannel.open()) {
            serverChannel.configureBlocking(true);
            LOGGER.log(Level.INFO, "Trying to connect to: " + serverAddress.toString());
            boolean isConnected = serverChannel.connect(serverAddress);
            if (!isConnected) {
                return;
            }
            LOGGER.log(Level.INFO, "Connected to: " + serverChannel.getRemoteAddress());

            var listener = new Thread(new ClientInputHandler(serverChannel));
            listener.start();

            var scanner = new Scanner(System.in);
            var writer = new MessageWriter(serverChannel);
            boolean userWantsToInput = true;
            while (userWantsToInput) {
                var message = scanner.nextLine();
                writer.addMessage(message);
                if (message.equals("STOP")) {
                    LOGGER.log(
                            Level.INFO,
                            "Client disconnects. But there still might be data to send. Don't close the program."
                    );
                    userWantsToInput = false;
                }
                try {
                    writer.writeAllToChannel();
                } catch (ConnectionLostException ex) {
                    listener.interrupt();
                    throw ex;
                }
            }
            LOGGER.log(Level.INFO, "All data sent. You can close the program now.");
        }
    }
}
