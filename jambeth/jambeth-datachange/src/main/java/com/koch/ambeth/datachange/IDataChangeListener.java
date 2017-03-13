package com.koch.ambeth.datachange;

import com.koch.ambeth.datachange.model.IDataChange;

public interface IDataChangeListener
{
	void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId);
}
