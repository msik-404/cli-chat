package com.msik404.clichat.message;

import java.nio.ByteBuffer;

public record Header(int messageLength) {

    public static int size() {
        return Integer.BYTES;
    }

    public void writeToBuffer(ByteBuffer buffer) {
        buffer.putInt(messageLength);
    }
}
