package com.prowolf.shared;

public interface TelegraphHandler {

    void onIncoming(boolean incoming);
    void onOutgoing(boolean outgoing);

}
