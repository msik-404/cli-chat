package com.msik404.clichat.message;

import java.nio.ByteBuffer;

public class MessageBufferHandler extends BufferHandler {

    public static int MAX_BUFFER_SIZE = MessageHeader.size() + 1024;

    /**
     * After calling this method header is cleared.
     *
     * @param buffer Buffer which is set for reading and has the message.
     * @param header Header which is set for reading the message.
     * @return String message
     */
    public static String getMessage(ByteBuffer buffer, MessageHeader header) {

        assert (!header.isBlank());

        int size = header.getMessageLength();
        header.clear();
        var builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append((char) buffer.get());
        }
        return builder.toString();
    }
}
