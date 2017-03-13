package com.koch.ambeth.merge.server.change;

import com.koch.ambeth.merge.server.service.IChangeAggregator;
import com.koch.ambeth.persistence.api.ITable;

public interface ITableChange extends Comparable<ITableChange>
{
	String getEntityHandlerName();

	ITable getTable();

	void setTable(ITable table);

	void addChangeCommand(IChangeCommand command);

	void addChangeCommand(ILinkChangeCommand command);

	void execute(IChangeAggregator changeAggreagator);
}