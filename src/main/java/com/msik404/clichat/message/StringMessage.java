package com.msik404.clichat.message;

import java.nio.charset.StandardCharsets;

public record StringMessage(String message) implements Message {

    public StringMessage(byte[] data) {
        this(new String(data, StandardCharsets.UTF_8));
    }

    @Override
    public byte[] getContents() {
        return message.getBytes(StandardCharsets.UTF_8);
    }

}
