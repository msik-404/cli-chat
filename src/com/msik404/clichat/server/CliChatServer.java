package com.msik404.clichat.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliChatServer {

    private static final Logger LOGGER = Logger.getLogger(CliChatServer.class.getName());

    private final ExecutorService pool;
    private final ServerSocket server;

    private final ConcurrentMap<SocketAddress, SocketWithOutput> outputs;

    // todo: create better exceptions.
    public CliChatServer(int port) throws RuntimeException {

        try {
            this.server = new ServerSocket(port);
            LOGGER.log(Level.INFO, "SERVER LISTENING AT: " + server.getLocalSocketAddress());

            this.pool = Executors.newCachedThreadPool();
            this.outputs = new ConcurrentHashMap<>();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    // todo: create better exceptions.
    public void run() throws RuntimeException {

        try {
            while (true) {
                var socket = server.accept();
                LOGGER.log(Level.INFO, "SERVER GOT CONNECTION FROM: " + socket.getRemoteSocketAddress());

                var output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                outputs.put(socket.getRemoteSocketAddress(), new SocketWithOutput(socket, output));

                pool.submit(new ClientHandler(socket, outputs));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
