package com.msik404.clichat.message;

import java.nio.ByteBuffer;

public class Header {

    private int messageLength;
    private boolean isEmpty;

    public Header() {
        this.messageLength = 0;
        this.isEmpty = true;
    }

    public static int size() {
        return Integer.BYTES;
    }

    public int getFrom(ByteBuffer buffer) {

        isEmpty = false;

        messageLength = buffer.getInt();
        return size();
    }

    public void getFrom(byte[] message) {

        messageLength = message.length;
        isEmpty = false;
    }

    public void putInto(ByteBuffer buffer) {
        buffer.putInt(messageLength);
    }

    public int getMessageLength() {
        return messageLength;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void clear() {
        isEmpty = true;
    }
}
