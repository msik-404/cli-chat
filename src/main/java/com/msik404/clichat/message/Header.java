package com.msik404.clichat.message;

import java.nio.ByteBuffer;

public interface Header {
    int getFrom(Message message);

    int getFrom(ByteBuffer buffer);

    void putInto(ByteBuffer buffer);
}
