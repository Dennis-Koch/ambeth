package de.osthus.ambeth.ioc.factory;

import de.osthus.ambeth.collections.ArrayList;

public class OrderState extends ArrayList<BeanConfigState>
{
	private int processedIndex = -1;

	public BeanConfigState consumeBeanConfigState()
	{
		if (processedIndex + 1 < size())
		{
			return get(++processedIndex);
		}
		return null;
	}
}
