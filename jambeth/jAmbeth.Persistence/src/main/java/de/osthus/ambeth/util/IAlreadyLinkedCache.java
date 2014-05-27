package de.osthus.ambeth.util;

import de.osthus.ambeth.persistence.ILink;

public interface IAlreadyLinkedCache
{
	void clear();

	boolean containsKey(ILink link, Object leftId, Object rightId);

	boolean removeKey(ILink link, Object leftId, Object rightId);

	boolean put(ILink link, Object[] recIdRecord);

	boolean put(ILink link, Object leftId, Object rightId);
}
