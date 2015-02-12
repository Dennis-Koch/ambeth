package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.config.Property;

public class DirectedLink implements IDirectedLink
{
	@Property
	protected IDirectedLinkMetaData metaData;

	@Property
	protected ITable fromTable;

	@Property
	protected ITable toTable;

	@Property
	protected ILink link;

	@Property
	protected IDirectedLink reverse;

	@Override
	public IDirectedLinkMetaData getMetaData()
	{
		return metaData;
	}

	@Override
	public ITable getFromTable()
	{
		return fromTable;
	}

	@Override
	public ITable getToTable()
	{
		return toTable;
	}

	@Override
	public ILink getLink()
	{
		return link;
	}

	@Override
	public IDirectedLink getReverseLink()
	{
		return reverse;
	}

	@Override
	public ILinkCursor findLinked(Object fromId)
	{
		return link.findLinked(this, fromId);
	}

	@Override
	public ILinkCursor findLinkedTo(Object toId)
	{
		return link.findLinkedTo(getReverseLink(), toId);
	}

	@Override
	public ILinkCursor findAllLinked(List<?> fromIds)
	{
		return link.findAllLinked(this, fromIds);
	}

	@Override
	public void linkIds(Object fromId, List<?> toIds)
	{
		link.linkIds(this, fromId, toIds);
	}

	@Override
	public void updateLink(Object fromId, Object toId)
	{
		link.updateLink(this, fromId, toId);
	}

	@Override
	public void unlinkIds(Object fromId, List<?> toIds)
	{
		link.unlinkIds(this, fromId, toIds);
	}

	@Override
	public void unlinkAllIds(Object fromId)
	{
		link.unlinkAllIds(this, fromId);
	}

	@Override
	public String toString()
	{
		return "DirectedLink: " + getMetaData().getName();
	}
}
