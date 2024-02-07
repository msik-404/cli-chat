package com.msik404.clichat.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageBufferHandler {

    public static int MAX_BUFFER_SIZE = Header.size() + 1024;

    public static int putMessage(ByteBuffer buffer, Header header, String message) {

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        header.updateFromMessage(data);

        header.putInto(buffer);
        buffer.put(data);

        return Header.size() + data.length;
    }

    public static String getMessage(ByteBuffer buffer, Header header) {

        int size = header.getMessageLength();
        var builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append((char) buffer.get());
        }
        return builder.toString();
    }
}
