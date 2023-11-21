package com.koch.ambeth.util.proxy;

public interface Factory {
    Callback[] getCallbacks();

    void setCallbacks(Callback[] callbacks);

    Callback getCallback(int index);

    void setCallback(int index, Callback callback);
}
