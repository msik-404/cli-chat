package com.msik404.clichat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliChatServer implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(CliChatServer.class.getName());

    private final ExecutorService pool;
    private final ServerSocket server;

    private final ConcurrentMap<SocketAddress, ClientOutputHandler> outputs;

    public CliChatServer(int port) throws IOException {

        this.server = new ServerSocket(port);
        LOGGER.log(Level.INFO, "SERVER LISTENING AT: " + server.getLocalSocketAddress());

        this.pool = Executors.newCachedThreadPool();
        this.outputs = new ConcurrentHashMap<>();
    }

    public void run() throws IOException {

        while (true) {
            var socket = server.accept();
            LOGGER.log(Level.INFO, "SERVER GOT CONNECTION FROM: " + socket.getRemoteSocketAddress());

            outputs.put(socket.getRemoteSocketAddress(), new ClientOutputHandler(socket));

            pool.submit(new ClientHandler(socket, outputs));
        }
    }

    @Override
    public void close() throws IOException {
        server.close();
        for (ClientOutputHandler s : outputs.values()) {
            if (!s.socket().isClosed()) {
                s.socket().close();
            }
        }
    }
}
