package com.koch.ambeth.util.threading;

public interface IGuiThreadHelper
{
	boolean isInGuiThread();

	void invokeInGuiAndWait(IBackgroundWorkerDelegate runnable);

	<R> R invokeInGuiAndWait(IResultingBackgroundWorkerDelegate<R> callback);

	<R, S> R invokeInGuiAndWait(IResultingBackgroundWorkerParamDelegate<R, S> callback, S state);

	void invokeInGuiAndWait(ISendOrPostCallback callback, Object state);

	void invokeInGui(IBackgroundWorkerDelegate runnable);

	void invokeInGui(ISendOrPostCallback callback, Object state);

	void invokeInGuiLate(IBackgroundWorkerDelegate callback);

	void invokeInGuiLate(ISendOrPostCallback callback, Object state);

	void invokeOutOfGui(IBackgroundWorkerDelegate runnable);
}
