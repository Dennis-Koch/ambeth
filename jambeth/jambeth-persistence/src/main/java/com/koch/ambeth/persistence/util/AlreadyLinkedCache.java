package com.koch.ambeth.persistence.util;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;

public class AlreadyLinkedCache implements IAlreadyLinkedCache, IDisposableBean
{
	protected final Tuple3KeyHashMap<ILink, Object, Object, Boolean> keyToObjectMap = new Tuple3KeyHashMap<ILink, Object, Object, Boolean>();

	@Override
	public void destroy() throws Throwable
	{
		clear();
	}

	@Override
	public void clear()
	{
		keyToObjectMap.clear();
	}

	@Override
	public boolean containsKey(ILink link, Object leftRecId, Object rightRecId)
	{
		return keyToObjectMap.containsKey(link, leftRecId, rightRecId);
	}

	@Override
	public boolean removeKey(ILink link, Object leftRecId, Object rightRecId)
	{
		Boolean value = keyToObjectMap.remove(link, leftRecId, rightRecId);
		return value != null;
	}

	@Override
	public boolean put(ILink link, Object[] recIdRecord)
	{
		return put(link, recIdRecord[0], recIdRecord[1]);
	}

	@Override
	public boolean put(ILink link, Object leftRecId, Object rightRecId)
	{
		return keyToObjectMap.putIfNotExists(link, leftRecId, rightRecId, Boolean.TRUE);
	}
}
