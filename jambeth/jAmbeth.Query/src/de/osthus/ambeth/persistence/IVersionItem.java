package de.osthus.ambeth.persistence;

import de.osthus.ambeth.util.IDisposable;

public interface IVersionItem extends IDisposable
{
	Object getId();

	Object getId(byte idIndex);

	Object getVersion();

	byte getAlternateIdCount();
}
