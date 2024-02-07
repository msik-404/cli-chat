package com.msik404.clichat.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageBufferHandler {

    public static int MAX_BUFFER_SIZE = Header.size() + 1024;

    public static void putMessage(ByteBuffer buffer, Header header, String message) {

        assert (header.isEmpty());

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        header.getFrom(data);

        header.putInto(buffer);
        buffer.put(data);
    }

    public static String getMessage(ByteBuffer buffer, Header header) {

        assert (!header.isEmpty());

        int size = header.getMessageLength();
        var builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append((char) buffer.get());
        }
        return builder.toString();
    }
}
