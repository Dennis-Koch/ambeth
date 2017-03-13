package com.koch.ambeth.datachange;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListener;

public class IdFilteredDataChangeListener extends UnfilteredDataChangeListener
{
	public static IDataChangeListener create(IDataChangeListener dataChangeListener, Object[] interestedIds)
	{
		IdFilteredDataChangeListener dcListener = new IdFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedIds = interestedIds;
		return dcListener;
	}

	public static IEventListener createEventListener(IDataChangeListener dataChangeListener, Object[] interestedIds)
	{
		IdFilteredDataChangeListener dcListener = new IdFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedIds = interestedIds;
		return dcListener;
	}

	protected Object[] interestedIds;

	public Object[] getInterestedIds()
	{
		return interestedIds;
	}

	public void setInterestedIds(Object[] interestedIds)
	{
		this.interestedIds = interestedIds;
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId)
	{
		if (dataChange.isEmpty())
		{
			return;
		}
		dataChange = dataChange.derive(interestedIds);
		if (dataChange.isEmpty())
		{
			return;
		}
		dataChangeListener.dataChanged(dataChange, dispatchTime, sequenceId);
	}
}
