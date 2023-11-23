package com.koch.ambeth.cache.mixin;

import java.util.concurrent.TimeUnit;

import com.koch.ambeth.util.state.IStateRollback;

/**
 * Intended to be used by an UI databinding framework to tell Ambeth the "touching" a lazy
 * relationship does not expect an immediate/synchronous resolution of a value holder. Instead it
 * permits Ambeth to queue up the request and fetch relations asynchronously. Via Java beans
 * property changes the UI databinding will be notified when the relations are initialized on the
 * monitored entity.
 */
public interface IAsyncLazyLoadController {

    IStateRollback pushAsynchronousResultAllowed();

    void awaitAsyncWorkload() throws InterruptedException;

    boolean awaitAsyncWorkload(long time, TimeUnit unit) throws InterruptedException;
}
