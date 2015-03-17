package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface IVersionItem extends IDisposable
{
	Object getId();

	Object getId(int idIndex);

	Object getVersion();

	int getAlternateIdCount();
}
