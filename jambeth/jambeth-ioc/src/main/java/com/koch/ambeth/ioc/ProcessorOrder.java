package com.koch.ambeth.ioc;

public enum ProcessorOrder
{
	DEFAULT(3), HIGHEST(0), HIGHER(1), HIGH(2), NORMAL(3), LOW(4), LOWER(5), LOWEST(6);

	private final int position;

	private ProcessorOrder(int position)
	{
		this.position = position;
	}

	public int getPosition()
	{
		return position;
	}
}
