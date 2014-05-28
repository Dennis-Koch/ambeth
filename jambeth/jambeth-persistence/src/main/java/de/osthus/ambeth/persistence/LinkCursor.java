package de.osthus.ambeth.persistence;

import de.osthus.ambeth.collections.IList;

public class LinkCursor extends BasicEnumerator<ILinkCursorItem> implements ILinkCursor, ILinkCursorItem
{
	protected IList<LinkCursorItem> items;
	protected int currentIndex = -1;

	protected byte fromIdIndex, toIdIndex;

	@Override
	public byte getFromIdIndex()
	{
		return fromIdIndex;
	}

	public void setFromIdIndex(byte fromIdIndex)
	{
		this.fromIdIndex = fromIdIndex;
	}

	@Override
	public byte getToIdIndex()
	{
		return toIdIndex;
	}

	public void setToIdIndex(byte toIdIndex)
	{
		this.toIdIndex = toIdIndex;
	}

	public void setItems(IList<LinkCursorItem> items)
	{
		this.items = items;
	}

	@Override
	public ILinkCursorItem getCurrent()
	{
		if (this.currentIndex == -1)
		{
			return null;
		}
		else
		{
			return this;
		}
	}

	@Override
	public boolean moveNext()
	{
		if (this.items.size() == this.currentIndex + 1)
		{
			return false;
		}
		currentIndex++;
		return true;
	}

	@Override
	public void reset()
	{
		this.currentIndex = -1;
	}

	@Override
	public void dispose()
	{
		this.items = null;
	}

	@Override
	public Object getFromId()
	{
		return this.items.get(this.currentIndex).getFromId();
	}

	@Override
	public Object getToId()
	{
		return this.items.get(this.currentIndex).getToId();
	}

}
