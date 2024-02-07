package com.msik404.clichat.client;

import com.msik404.clichat.message.MessageBuffer;

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

    public void run() throws IOException {

        try (var serverChannel = SocketChannel.open()) {
            serverChannel.configureBlocking(true);
            LOGGER.log(Level.INFO, "Trying to connect to the server: %s", serverAddress);
            boolean isConnected = serverChannel.connect(serverAddress);
            if (!isConnected) {
                return;
            }
            LOGGER.log(Level.INFO, "Connection to server: %s was successful.", serverAddress);

            var listener = new Thread(new ClientInputHandler(serverChannel));
            listener.start();

            var buffer = new MessageBuffer();
            var scanner = new Scanner(System.in);
            boolean userWantsToInput = true;
            int toWriteAmount = -1;
            while (userWantsToInput || toWriteAmount != 0) {
                if (userWantsToInput) {
                    var message = scanner.nextLine();
                    buffer.write(message);
                    buffer.flip();
                    if (message.equals("STOP")) {
                        listener.interrupt();
                        LOGGER.log(Level.INFO, "Client disconnects. But there still might be data to send. " +
                                "Don't close the program."
                        );
                        userWantsToInput = false;
                    }
                }
                toWriteAmount = buffer.writeToChannel(serverChannel);
                if (userWantsToInput) {
                    if (toWriteAmount != 0) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                    }
                }
            }
            LOGGER.log(Level.INFO, "All data sent. You can close the program now.");
        }
    }
}
