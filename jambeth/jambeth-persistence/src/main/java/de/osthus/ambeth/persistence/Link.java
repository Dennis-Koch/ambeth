package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.ParamChecker;

public class Link implements ILink, IInitializingBean
{
	protected ITable fromTable;

	protected ITable toTable;

	protected boolean nullable = false;

	protected IDirectedLink directedLink;

	protected IDirectedLink reverseDirectedLink;

	protected IAlreadyLinkedCache alreadyLinkedCache;

	protected String name;

	protected String tableName;

	protected String archiveTableName;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(fromTable, "fromTable");
		ParamChecker.assertNotNull(directedLink, "directedLink");
		ParamChecker.assertTrue(toTable != null || directedLink.getToMember() != null, "toTable or toMember");
		ParamChecker.assertNotNull(reverseDirectedLink, "reverseDirectedLink");
		ParamChecker.assertNotNull(alreadyLinkedCache, "alreadyLinkedCache");
	}

	public void setAlreadyLinkedCache(IAlreadyLinkedCache alreadyLinkedCache)
	{
		this.alreadyLinkedCache = alreadyLinkedCache;
	}

	@Override
	public ITable getFromTable()
	{
		return fromTable;
	}

	public void setFromTable(ITable fromTable)
	{
		this.fromTable = fromTable;
	}

	@Override
	public ITable getToTable()
	{
		return toTable;
	}

	public void setToTable(ITable toTable)
	{
		this.toTable = toTable;
	}

	@Override
	public IField getFromField()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IField getToField()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isNullable()
	{
		return nullable;
	}

	@Override
	public boolean hasLinkTable()
	{
		return directedLink.isStandaloneLink() && reverseDirectedLink.isStandaloneLink();
	}

	public void setNullable(boolean nullable)
	{
		this.nullable = nullable;
	}

	@Override
	public IDirectedLink getDirectedLink()
	{
		return directedLink;
	}

	public void setDirectedLink(IDirectedLink directedLink)
	{
		this.directedLink = directedLink;
	}

	@Override
	public IDirectedLink getReverseDirectedLink()
	{
		return reverseDirectedLink;
	}

	public void setReverseDirectedLink(IDirectedLink reverseDirectedLink)
	{
		this.reverseDirectedLink = reverseDirectedLink;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getTableName()
	{
		if (hasLinkTable())
		{
			return name;
		}
		else
		{
			return tableName;
		}
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	@Override
	public String getFullqualifiedEscapedTableName()
	{
		return getTableName();
	}

	@Override
	public String getArchiveTableName()
	{
		return archiveTableName;
	}

	public void setArchiveTableName(String archiveTableName)
	{
		this.archiveTableName = archiveTableName;
	}

	@Override
	public ILinkCursor findAllLinked(IDirectedLink fromLink, List<?> fromIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ILinkCursor findAllLinkedTo(IDirectedLink fromLink, List<?> toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ILinkCursor findLinked(IDirectedLink fromLink, Object fromId)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ILinkCursor findLinkedTo(IDirectedLink fromLink, Object toId)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void linkIds(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void updateLink(IDirectedLink fromLink, Object fromId, Object toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void unlinkIds(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void unlinkAllIds(IDirectedLink fromLink, Object fromId)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void unlinkAllIds()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected boolean addLinkModToCache(IDirectedLink fromLink, Object fromId, Object toId)
	{
		Object leftId, rightId;
		if (getDirectedLink() == fromLink)
		{
			leftId = fromId;
			rightId = toId;
		}
		else if (getReverseDirectedLink() == fromLink)
		{
			leftId = toId;
			rightId = fromId;
		}
		else
		{
			throw new IllegalArgumentException("Invalid link " + fromLink);
		}
		return alreadyLinkedCache.put(this, leftId, rightId);
	}

	@Override
	public String toString()
	{
		return "Link: " + getName();
	}

	@Override
	public void startBatch()
	{
		// Intended blank
	}

	@Override
	public int[] finishBatch()
	{
		return new int[0];
	}

	@Override
	public void clearBatch()
	{
		// Intended blank
	}
}
