package com.msik404.clichat.server;

import java.io.ObjectOutputStream;
import java.net.Socket;

public record SocketWithOutput(Socket socket, ObjectOutputStream output) {
}
