package com.msik404.clichat.message;

import java.nio.ByteBuffer;

public abstract class BufferHandler {

    /**
     * @param buffer  Buffer with enough space to put in message with header.
     * @param header  Reusable header, after calling this method it will be blank.
     * @param message String message to be placed with header in buffer.
     */
    public static void putMessage(ByteBuffer buffer, ReusableHeader header, Message message) {

        header.getFrom(message);
        header.putInto(buffer);
        header.clear();
        buffer.put(message.getContents());
    }
}
