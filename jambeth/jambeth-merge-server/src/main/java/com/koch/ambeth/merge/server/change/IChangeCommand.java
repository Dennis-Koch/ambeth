package com.koch.ambeth.merge.server.change;

import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface IChangeCommand
{
	void configureFromContainer(IChangeContainer changeContainer, ITable table);

	IObjRef getReference();

	IChangeCommand addCommand(IChangeCommand other);
}
