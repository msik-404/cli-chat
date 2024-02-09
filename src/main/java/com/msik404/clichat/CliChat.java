package com.msik404.clichat;

import com.msik404.clichat.client.CliChatClient;
import com.msik404.clichat.message.ConnectionLostException;
import com.msik404.clichat.server.CliChatServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CliChat {

    private static final Logger LOGGER = Logger.getLogger(CliChatServer.class.getName());

    public static void main(String[] args) throws IOException {

        if (args[0].equals("client")) {
            String host = args[1];
            InetAddress address = InetAddress.getByName(host);
            int port = Integer.parseInt(args[2]);
            var client = new CliChatClient(new InetSocketAddress(address, port));
            try {
                client.run();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Client closed due to error: " + ex.getMessage());
            } catch (ConnectionLostException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            }
        } else if (args[0].equals("server")) {
            int port = Integer.parseInt(args[1]);
            try (var server = new CliChatServer(port)) {
                server.run();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Server closed due to error: " + ex.getMessage());
            }
        }
    }
}
