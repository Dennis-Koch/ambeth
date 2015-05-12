package de.osthus.ambeth.persistence;


public interface IVersionItem
{
	Object getId();

	Object getId(int idIndex);

	Object getVersion();

	int getAlternateIdCount();
}
