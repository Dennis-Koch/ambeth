package de.osthus.ambeth.util;

public class InterruptingParamHolder extends ParamHolder<Throwable>
{
	protected final Thread mainThread;

	public InterruptingParamHolder(Thread mainThread)
	{
		this.mainThread = mainThread;
	}

	@Override
	public void setValue(Throwable value)
	{
		super.setValue(value);

		if (value != null)
		{
			// necessary to inform the main thread that it should not wait any longer for the latch
			mainThread.interrupt();
		}
	}
}