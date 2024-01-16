package com.msik404.clichat;

import com.msik404.clichat.client.CliChatClient;
import com.msik404.clichat.server.CliChatServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CliChat {

    public static void main(String[] args) throws UnknownHostException {

        if (args[0].equals("client")) {
            String host = args[1];
            InetAddress address = InetAddress.getByName(host);
            int port = Integer.parseInt(args[2]);
            var client = new CliChatClient(address, port);
            client.run();
        } else if(args[0].equals("server")) {
            int port = Integer.parseInt(args[1]);
            var server = new CliChatServer(port);
            server.run();
        }
    }
}
