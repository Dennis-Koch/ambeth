package de.osthus.ambeth.merge;

public interface IValueObjectConfigExtendable
{
	void registerValueObjectConfig(IValueObjectConfig config);

	void unregisterValueObjectConfig(IValueObjectConfig config);
}
