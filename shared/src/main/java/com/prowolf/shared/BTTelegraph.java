package com.prowolf.shared;

public abstract class BTTelegraph extends Telegraph {

    public BTTelegraph(TelegraphHandler telegraphHandler) {
        super(telegraphHandler);
    }

    @Override
    protected boolean doConnect(Object target) {
        return false;
    }

    @Override
    protected void doDisconnect() {

    }

}
