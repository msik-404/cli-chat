package com.msik404.clichat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliChatServer implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(CliChatServer.class.getName());

    private final ExecutorService pool;
    private final ServerSocketChannel server;
    private final InetSocketAddress serverAddress;

    private final ConcurrentMap<SocketAddress, ClientOutputHandler> outputs;

    public CliChatServer(int port) throws IOException {

        this.server = ServerSocketChannel.open();
        server.configureBlocking(true);
        this.serverAddress = new InetSocketAddress(port);

        this.pool = Executors.newCachedThreadPool();
        this.outputs = new ConcurrentHashMap<>();
    }

    public void run() throws IOException {

        server.bind(serverAddress);
        LOGGER.log(Level.INFO, "SERVER LISTENING AT: " + server.getLocalAddress());

        while (true) {
            var socketChannel = server.accept();
            socketChannel.configureBlocking(true);
            LOGGER.log(Level.INFO, "SERVER GOT CONNECTION FROM: " + socketChannel.getRemoteAddress());

            outputs.put(socketChannel.getRemoteAddress(), new ClientOutputHandler(socketChannel));

            pool.submit(new ClientHandler(socketChannel, outputs));
        }
    }

    @Override
    public void close() throws IOException {
        if (server.isOpen()) {
            server.close();
        }
        for (ClientOutputHandler s : outputs.values()) {
            if (s.socketChannel().isOpen()) {
                s.socketChannel().close();
            }
        }
    }
}
