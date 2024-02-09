package com.msik404.clichat.message;

import java.nio.ByteBuffer;

public class MessageHeader extends ReusableHeader {

    private int messageLength;

    public MessageHeader() {
        this.messageLength = 0;
    }

    public static int size() {
        return Integer.BYTES;
    }

    @Override
    public int getFrom(Message message) {

        messageLength = message.getContents().length;
        super.isBlank = false;
        return size();
    }

    @Override
    public int getFrom(ByteBuffer buffer) {

        messageLength = buffer.getInt();
        super.isBlank = false;
        return size();
    }

    @Override
    public void putInto(ByteBuffer buffer) {
        buffer.putInt(messageLength);
    }

    public int getMessageLength() {
        return messageLength;
    }
}
