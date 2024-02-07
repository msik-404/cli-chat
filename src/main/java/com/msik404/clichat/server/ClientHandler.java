package com.msik404.clichat.server;

import com.msik404.clichat.message.Header;
import com.msik404.clichat.message.MessageBufferHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final SocketChannel socketChannel;
    private final ConcurrentMap<SocketAddress, ClientOutputHandler> outputs;

    public ClientHandler(SocketChannel socketChannel, ConcurrentMap<SocketAddress, ClientOutputHandler> outputs) {

        this.socketChannel = socketChannel;
        this.outputs = outputs;
    }

    @Override
    public void run() {

        var buffer = ByteBuffer.allocate(Header.size() + MessageBufferHandler.MAX_BUFFER_SIZE);
        int bufferedAmount = 0;
        boolean flipped = false;

        var header = new Header();

        boolean userWantsToInput = true;
        boolean stopSending = false;

        try {
            do {
                bufferedAmount += socketChannel.read(buffer);

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
                    String message = MessageBufferHandler.getMessage(buffer, header);
                    bufferedAmount -= header.getMessageLength();
                    header.reset();
                    if (bufferedAmount == 0) {
                        buffer.clear();
                    } else {
                        buffer.compact();
                    }

                    if (message.equals("STOP")) {
                        userWantsToInput = false;
                    }

                    for (ClientOutputHandler clientOutputHandler : outputs.values()) {
                        var otherChannel = clientOutputHandler.socketChannel();
                        if (!socketChannel.equals(otherChannel)) {
                            try {
                                clientOutputHandler.sendMessage(socketChannel.getRemoteAddress(), header, message);
                            } catch (IOException ex) {
                                outputs.remove(otherChannel.getRemoteAddress());
                                LOGGER.log(Level.WARNING, "Write to client has failed. Probably client's socket has been closed.");
                            }
                            catch (InterruptedException e) {
                                LOGGER.log(Level.SEVERE, "Current task has been interrupted from outside while waiting for mutex to send message to client.");
                                stopSending = true;
                                break;
                            }
                        }
                    }
                }
            } while (userWantsToInput && !stopSending);

        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Read from client has failed. Probably client has left unexpectedly.");
        } finally {
            try {
                // When client wishes to exit or some error happened, remove client from message receivers.
                // Only leaving clients close sockets.
                outputs.remove(socketChannel.getRemoteAddress());
                if (socketChannel.isOpen()) {
                    socketChannel.close();
                }
                LOGGER.log(Level.INFO, "User: " + socketChannel.getRemoteAddress() + " has disconnected.");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Socket threw exception during attempt of closing it.");
            }
        }
    }
}
