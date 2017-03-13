package com.koch.ambeth.merge;

import com.koch.ambeth.service.merge.IValueObjectConfig;

public interface IValueObjectConfigExtendable
{
	void registerValueObjectConfig(IValueObjectConfig config);

	void unregisterValueObjectConfig(IValueObjectConfig config);
}
