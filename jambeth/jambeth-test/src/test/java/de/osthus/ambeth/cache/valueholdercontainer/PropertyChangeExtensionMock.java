package de.osthus.ambeth.cache.valueholdercontainer;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.databinding.IPropertyChangeExtension;

public class PropertyChangeExtensionMock implements IPropertyChangeExtension
{
	protected final IMap<String, Integer> propertyNameToHitCountMap;

	public PropertyChangeExtensionMock(IMap<String, Integer> propertyNameToHitCountMap)
	{
		this.propertyNameToHitCountMap = propertyNameToHitCountMap;
	}

	@Override
	public void propertyChanged(Object obj, String propertyName, Object oldValue, Object currentValue)
	{
		Integer hitCount = propertyNameToHitCountMap.get(propertyName);
		if (hitCount == null)
		{
			hitCount = Integer.valueOf(0);
		}
		hitCount++;
		propertyNameToHitCountMap.put(propertyName, hitCount);
	}
}