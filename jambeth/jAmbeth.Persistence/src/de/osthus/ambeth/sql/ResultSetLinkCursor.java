package de.osthus.ambeth.sql;

import de.osthus.ambeth.persistence.ILinkCursor;
import de.osthus.ambeth.persistence.ILinkCursorItem;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class ResultSetLinkCursor implements ILinkCursor, ILinkCursorItem, IDisposable
{
	protected IResultSet resultSet;

	protected Object fromId, toId;

	protected byte fromIdIndex, toIdIndex;

	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(this.resultSet, "ResultSet");
	}

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

	public IResultSet getResultSet()
	{
		return resultSet;
	}

	public void setResultSet(IResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	@Override
	public Object getFromId()
	{
		return this.fromId;
	}

	@Override
	public Object getToId()
	{
		return this.toId;
	}

	@Override
	public boolean moveNext()
	{
		if (this.resultSet.moveNext())
		{
			this.fromId = this.resultSet.getCurrent()[0];
			this.toId = this.resultSet.getCurrent()[1];
			return true;
		}
		return false;
	}

	@Override
	public ILinkCursorItem getCurrent()
	{
		return this;
	}

	@Override
	public void dispose()
	{
		if (this.resultSet != null)
		{
			this.resultSet.dispose();
			this.resultSet = null;
		}
		this.fromId = null;
		this.toId = null;
	}

}
