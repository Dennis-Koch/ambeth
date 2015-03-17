package de.osthus.ambeth.change;

import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.service.IChangeAggregator;

public interface ITableChange extends Comparable<ITableChange>
{
	String getEntityHandlerName();

	ITable getTable();

	void setTable(ITable table);

	void addChangeCommand(IChangeCommand command);

	void addChangeCommand(ILinkChangeCommand command);

	void execute(IChangeAggregator changeAggreagator);
}