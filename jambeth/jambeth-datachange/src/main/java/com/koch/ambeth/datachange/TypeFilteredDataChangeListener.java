package com.koch.ambeth.datachange;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListener;

public class TypeFilteredDataChangeListener extends UnfilteredDataChangeListener
{
	public static IDataChangeListener create(IDataChangeListener dataChangeListener, Class<?>[] interestedTypes)
	{
		TypeFilteredDataChangeListener dcListener = new TypeFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedTypes = interestedTypes;
		return dcListener;
	}

	public static IEventListener createEventListener(IDataChangeListener dataChangeListener, Class<?>[] interestedTypes)
	{
		TypeFilteredDataChangeListener dcListener = new TypeFilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		dcListener.interestedTypes = interestedTypes;
		return dcListener;
	}

	protected Class<?>[] interestedTypes;

	public Class<?>[] getInterestedTypes()
	{
		return interestedTypes;
	}

	public void setInterestedTypes(Class<?>[] interestedTypes)
	{
		this.interestedTypes = interestedTypes;
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId)
	{
		if (dataChange.isEmpty())
		{
			return;
		}
		dataChange = dataChange.derive(interestedTypes);
		if (dataChange.isEmpty())
		{
			return;
		}
		dataChangeListener.dataChanged(dataChange, dispatchTime, sequenceId);
	}
}
