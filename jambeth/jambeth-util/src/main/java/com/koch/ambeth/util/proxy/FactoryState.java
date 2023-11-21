package com.koch.ambeth.util.proxy;

public interface FactoryState {
    Callback[] getCallbacksShared();
    Callback getCallback(int index);
    void setCallbacks(Callback[] callbacks);
}
