package com.msik404.clichat.client;

import com.msik404.clichat.message.Header;
import com.msik404.clichat.message.MessageBufferHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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

        var buffer = ByteBuffer.allocate(Header.size() + MessageBufferHandler.MAX_BUFFER_SIZE);
        int bufferedAmount = 0;
        var header = new Header();

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
            boolean userWantsToInput = true;
            while (userWantsToInput || bufferedAmount != 0) {
                if (userWantsToInput) {
                    var message = scanner.nextLine();
                    bufferedAmount += MessageBufferHandler.putMessage(buffer, header, message);
                    buffer.flip();
                    if (message.equals("STOP")) {
                        LOGGER.log(Level.INFO, "Client disconnects. But there still might be data to send. Don't close the program.");
                        userWantsToInput = false;
                    }
                }
                bufferedAmount -= serverChannel.write(buffer);
                if (userWantsToInput) {
                    if (bufferedAmount != 0) {
                        buffer.compact();
                    } else {
                        buffer.clear();
                    }
                }
            }
            listener.interrupt();
            LOGGER.log(Level.INFO, "All data sent. You can close the program now.");
        }
    }
}
