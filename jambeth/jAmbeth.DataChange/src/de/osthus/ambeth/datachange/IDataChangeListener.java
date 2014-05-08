package de.osthus.ambeth.datachange;

import de.osthus.ambeth.datachange.model.IDataChange;

public interface IDataChangeListener
{
	void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId);
}
