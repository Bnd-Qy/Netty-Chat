package com.vmware.message;

public class PingMessage extends Message {
    @Override
    public int getMessageType() {
        return PingRequestMessage;
    }
}
