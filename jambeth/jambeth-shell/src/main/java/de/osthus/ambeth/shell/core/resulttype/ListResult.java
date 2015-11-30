package de.osthus.ambeth.shell.core.resulttype;

import java.util.ArrayList;

public class ListResult<T> extends CommandResult
{

	private java.util.List<T> list;

	/**
	 * add a record to the instance
	 *
	 * @param record
	 *            instance of class that extends {@link CommandResult}
	 */
	public void addRecord(T record)
	{
		if (list == null)
		{
			list = new ArrayList<T>();
		}
		list.add(record);
	}

	/**
	 * remove a record from the instance
	 *
	 * @param record
	 *            instance of class that extends {@link CommandResult}
	 */
	public void removeRecord(T record)
	{
		if (list != null)
		{
			list.remove(record);
		}
	}

	/**
	 * get all the records from the instance
	 * <p>
	 * the result is a instance of java.util.List, which contains instances of class that extends {@link CommandResult}.
	 * </p>
	 * <p>
	 * the {@link CommandResult} instances can be mixture of all its subClass.
	 * </p>
	 *
	 * @return {@link ListResult}
	 */
	public java.util.List<T> getAllRecords()
	{
		return list;
	}

	@Override
	public String toString()
	{
		StringBuffer strBuf = new StringBuffer();
		for (T commandResult : list)
		{
			strBuf.append(commandResult.toString());
		}
		return strBuf.toString();
	}
}
