package de.osthus.ambeth.change;

import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.service.IChangeAggregator;
import de.osthus.ambeth.util.IDisposable;

public interface ITableChange extends IDisposable, Comparable<ITableChange>
{
	String getEntityHandlerName();

	ITable getTable();

	void setTable(ITable table);

	void addChangeCommand(IChangeCommand command);

	void addChangeCommand(ILinkChangeCommand command);

	void execute(IChangeAggregator changeAggreagator);
}