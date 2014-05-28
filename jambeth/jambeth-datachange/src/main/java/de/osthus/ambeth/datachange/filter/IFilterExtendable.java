package de.osthus.ambeth.datachange.filter;

public interface IFilterExtendable
{
	void registerFilter(IFilter filter, String topic);

	void unregisterFilter(IFilter filter, String topic);
}
