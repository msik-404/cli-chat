package com.msik404.clichat.message;

public abstract class ReusableHeader implements Header {

    protected boolean isBlank;

    public ReusableHeader() {
        this.isBlank = true;
    }

    public void clear() {
        isBlank = true;
    }

    public boolean isBlank() {
        return isBlank;
    }
}
