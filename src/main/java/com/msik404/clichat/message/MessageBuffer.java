package com.msik404.clichat.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MessageBuffer {

    public static int MAX_BUFFER_CONTENT_SIZE = Header.size() + 1024;

    private final ByteBuffer buffer;
    private int readAmount;
    private int toWriteAmount;

    public MessageBuffer() {

        this.buffer = ByteBuffer.allocate(MAX_BUFFER_CONTENT_SIZE);
        this.readAmount = 0;
        this.toWriteAmount = 0;
    }

    public void write(String message) {

        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        var header = new Header(data.length);

        header.writeToBuffer(buffer);
        buffer.put(data);

        toWriteAmount += Header.size() + data.length;
    }

    public int writeToChannel(SocketChannel channel) throws IOException {
        toWriteAmount -= channel.write(buffer);
        return  toWriteAmount;
    }

    public void flip() {
        buffer.flip();
    }

    public void clear() {
        buffer.clear();
    }

    public void compact() {
        buffer.compact();
    }

    public void readFromChannel(SocketChannel channel) throws IOException {
        readAmount += channel.read(buffer);
    }

    public Optional<Header> readHeader() {
        if (readAmount >= Header.size()) {
            readAmount -= Header.size();
            return Optional.of(new Header(buffer.getInt()));
        }
        return Optional.empty();
    }

    public Optional<String> readMessage(Header header) {

        int size = header.messageLength();
        if (readAmount < size) {
            return Optional.empty();
        }
        var builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            builder.append(buffer.get());
        }
        readAmount -= size;
        return Optional.of(builder.toString());
    }
}
