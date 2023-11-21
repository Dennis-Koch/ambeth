package com.koch.ambeth.util.proxy;

import java.lang.reflect.Method;

public interface NoOp extends Callback {
    public static final NoOp INSTANCE = new NoOp() {};
}
