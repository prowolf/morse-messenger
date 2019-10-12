package com.prowolf.shared;

public abstract class Telegraph {

    private boolean connected;
    private boolean incoming;
    private boolean outgoing;
    private TelegraphHandler telegraphHandler;

    public Telegraph(TelegraphHandler telegraphHandler) {
        connected = false;
        incoming = false;
        outgoing = false;
        this.telegraphHandler = telegraphHandler;
    }

    public final boolean connect(Object target) {
        if (connected) {
            disconnect();
        }

        return connected = doConnect(target);
    }

    abstract protected boolean doConnect(Object target);

    public final void disconnect() {
        if (connected) {
            setIncoming(false);
            setOutgoing(false);
            doDisconnect();
            connected = false;
        }
    }

    abstract protected void doDisconnect();

    public final boolean isConnected() {
        return connected;
    }

    public final boolean isIncoming() {
        return incoming;
    }

    protected final void setIncoming(boolean incoming) {
        this.incoming = incoming;
        telegraphHandler.onIncoming(incoming);
    }

    public final boolean isOutgoing() {
        return outgoing;
    }

    public final void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
        onOutgoing(outgoing);
        telegraphHandler.onOutgoing(outgoing);
    }

    abstract protected void onOutgoing(boolean outgoing);

}
