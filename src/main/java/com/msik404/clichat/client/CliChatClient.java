package com.msik404.clichat.client;

import com.msik404.clichat.message.MessageBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
            serverChannel.configureBlocking(false);
            LOGGER.log(Level.INFO, "Trying to connect to the server: %s", serverAddress);
            boolean isConnected = serverChannel.connect(serverAddress);
            if (isConnected) {
                LOGGER.log(Level.INFO, "Connection to server: %s was successful.", serverAddress);
            }

            var selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_CONNECT);
            serverChannel.register(selector, SelectionKey.OP_WRITE);

            Thread listener = new Thread(new ClientInputHandler(serverChannel));
            listener.start();

            var buffer = new MessageBuffer();
            var scanner = new Scanner(System.in);
            boolean userWantsToInput = true;
            int toWriteAmount = -1;
            while (userWantsToInput && toWriteAmount != 0) {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
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
                    if (key.isConnectable()) {
                        serverChannel.finishConnect();
                        LOGGER.log(Level.INFO, "Connection to server: %s was successful.", serverAddress);
                        key.cancel();
                    }
                    if (key.isWritable()) {
                        toWriteAmount = buffer.writeToChannel(serverChannel);
                        if (userWantsToInput) {
                            if (toWriteAmount != 0) {
                                buffer.compact();
                            } else {
                                buffer.clear();
                            }
                        }
                    }
                }
            }
            LOGGER.log(Level.INFO, "All data sent. You can close the program now.");
        }
    }
}
