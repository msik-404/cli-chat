package com.msik404.clichat.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CliChatClient {

    private final ExecutorService pool;
    private final Socket server;

    public CliChatClient(InetAddress address, int port) throws RuntimeException {

        try {
            this.server = new Socket(address, port);
            this.pool = Executors.newCachedThreadPool();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void run() throws RuntimeException {

        try {
            var output = new ObjectOutputStream(server.getOutputStream());
            var input = new ObjectInputStream(server.getInputStream());

            pool.submit(() -> {
                while (true) {
                    var message = (String) input.readObject();
                    System.out.println(message);
                }
            });

            var scanner = new Scanner(System.in);

            while (true) {
                var message = scanner.next();
                if (message.equals("STOP")) {
                    output.writeObject(null);
                    break;
                }
                output.writeObject(message);
                output.flush();
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            try {
                server.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
    }
}
