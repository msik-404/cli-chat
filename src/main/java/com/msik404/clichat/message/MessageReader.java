package com.msik404.clichat.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class MessageReader {

    private final SocketChannel channel;
    private final ByteBuffer buffer;
    private final MessageHeader header;
    private final ArrayList<String> messageBuffer;
    private int bufferedAmount;

    public MessageReader(SocketChannel channel) {

        this.channel = channel;
        this.buffer = ByteBuffer.allocate(MessageBufferHandler.MAX_BUFFER_SIZE);
        this.header = new MessageHeader();
        this.messageBuffer = new ArrayList<>();
        this.bufferedAmount = 0;
    }

    private void clear() {
        if (buffer.hasRemaining()) {
            buffer.compact();
        } else {
            buffer.clear();
        }
    }

    public void readFromChannel() throws IOException, ConnectionLostException {

        int count = channel.read(buffer);
        if (count == -1) {
            throw new ConnectionLostException();
        }
        bufferedAmount += count;

        buffer.flip();
        // repeat while more messages can be read.
        do {
            if (header.isBlank() && bufferedAmount >= MessageHeader.size()) {
                bufferedAmount -= header.getFrom(buffer);
            }
            if (!header.isBlank() && bufferedAmount >= header.getMessageLength()) {
                bufferedAmount -= header.getMessageLength();
                messageBuffer.add(MessageBufferHandler.getMessage(buffer, header));
            }
        } while (header.isBlank() && bufferedAmount >= MessageHeader.size());

        clear();
    }

    public ArrayList<String> getMessageBuffer() {
        return messageBuffer;
    }

}
