package com.msik404.clichat.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageWriter {

    private final SocketChannel channel;
    private final ByteBuffer buffer;
    private final MessageHeader header;
    private int bufferedAmount;
    private boolean writeMode;

    public MessageWriter(SocketChannel channel) {

        this.channel = channel;
        this.buffer = ByteBuffer.allocate(MessageBufferHandler.MAX_BUFFER_SIZE);
        this.bufferedAmount = 0;
        this.writeMode = true;
        this.header = new MessageHeader();
    }

    /**
     * @param message String to be sent to server.
     * @return Boolean indicating buffer had space for the message.
     */
    public boolean addMessage(String message) {

        if (!writeMode) {
            buffer.flip();
            writeMode = true;
        }

        var mess = new StringMessage(message);
        header.getFrom(mess);
        int byteAmount = mess.getContents().length + MessageHeader.size();
        if (byteAmount + bufferedAmount > MessageBufferHandler.MAX_BUFFER_SIZE) {
            header.clear();
            return false;
        }
        MessageBufferHandler.putMessage(buffer, header, mess);
        bufferedAmount += byteAmount;

        return true;
    }

    private void clear() {
        if (buffer.hasRemaining()) {
            buffer.compact();
        } else {
            buffer.clear();
        }
    }

    public int writeToChannel() throws IOException, ConnectionLostException {

        assert (bufferedAmount != 0);

        if (writeMode) {
            buffer.flip();
            writeMode = true;
        }

        int count = channel.write(buffer);
        if (count == -1) {
            clear();
            throw new ConnectionLostException();
        }
        bufferedAmount -= count;
        clear();

        return count;
    }

    public int writeAllToChannel() throws IOException, ConnectionLostException {

        assert (bufferedAmount != 0);

        if (writeMode) {
            buffer.flip();
            writeMode = true;
        }

        int count;
        while (buffer.hasRemaining()) {
            count = channel.write(buffer);
            if (count == -1) {
                clear();
                throw new ConnectionLostException();
            }
        }
        clear();

        count = bufferedAmount;
        bufferedAmount = 0;
        return count;
    }

}
