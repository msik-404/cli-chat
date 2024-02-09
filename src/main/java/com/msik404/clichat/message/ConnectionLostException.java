package com.msik404.clichat.message;

public class ConnectionLostException extends RuntimeException {

    public ConnectionLostException() {
        super("Connection has been lost with the other side.");
    }

}
