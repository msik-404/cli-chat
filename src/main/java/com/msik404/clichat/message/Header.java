package com.msik404.clichat.message;

import java.nio.ByteBuffer;

public class Header {

    private int messageLength;
    private boolean justUpdated;

    public Header() {
        this.messageLength = 0;
        this.justUpdated = false;
    }

    public static int size() {
        return Integer.BYTES;
    }

    public int updateFromBuffer(ByteBuffer buffer) {
        messageLength = buffer.getInt();
        justUpdated = true;
        return size();
    }

    public void updateFromMessage(byte[] message) {
        messageLength = message.length;
        justUpdated = true;
    }

    public void putInto(ByteBuffer buffer) {
        buffer.putInt(messageLength);
    }

    public int getMessageLength() {
        return messageLength;
    }

    public boolean isJustUpdated() {
        return justUpdated;
    }

    public void reset() {
        messageLength = 0;
        justUpdated = false;
    }
}
