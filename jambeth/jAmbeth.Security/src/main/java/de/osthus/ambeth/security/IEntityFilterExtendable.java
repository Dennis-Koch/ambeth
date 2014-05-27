package de.osthus.ambeth.security;

public interface IEntityFilterExtendable
{
	void registerEntityFilter(IEntityFilter entityFilter);

	void unregisterEntityFilter(IEntityFilter entityFilter);
}
