package de.osthus.ambeth.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.persistence.ILink;

public class AlreadyLinkedCache implements IAlreadyLinkedCache, IDisposableBean
{
	protected final Map<ILink, Set<LinkTuple>> keyToObjectMap = new HashMap<ILink, Set<LinkTuple>>();

	@Override
	public void destroy() throws Throwable
	{
		clear();
	}

	@Override
	public void clear()
	{
		this.keyToObjectMap.clear();
	}

	@Override
	public boolean containsKey(ILink link, Object leftRecId, Object rightRecId)
	{
		Set<LinkTuple> linkTuples = this.keyToObjectMap.get(link);
		if (linkTuples == null)
		{
			return false;
		}
		LinkTuple linkTuple = new LinkTuple();
		linkTuple.leftRecId = leftRecId;
		linkTuple.rightRecId = rightRecId;
		return linkTuples.contains(linkTuple);
	}

	@Override
	public boolean removeKey(ILink link, Object leftRecId, Object rightRecId)
	{
		Set<LinkTuple> linkTuples = this.keyToObjectMap.get(link);
		if (linkTuples == null)
		{
			return false;
		}
		LinkTuple linkTuple = new LinkTuple();
		linkTuple.leftRecId = leftRecId;
		linkTuple.rightRecId = rightRecId;
		return linkTuples.remove(linkTuple);
	}

	@Override
	public boolean put(ILink link, Object[] recIdRecord)
	{
		return this.put(link, recIdRecord[0], recIdRecord[1]);
	}

	@Override
	public boolean put(ILink link, Object leftRecId, Object rightRecId)
	{
		Set<LinkTuple> linkTuples = keyToObjectMap.get(link);
		if (linkTuples == null)
		{
			linkTuples = new HashSet<LinkTuple>();
			keyToObjectMap.put(link, linkTuples);
		}
		LinkTuple linkTuple = new LinkTuple();
		linkTuple.leftRecId = leftRecId;
		linkTuple.rightRecId = rightRecId;
		if (linkTuples.contains(linkTuple))
		{
			return false;
		}
		linkTuples.add(linkTuple);

		return true;
	}
}
