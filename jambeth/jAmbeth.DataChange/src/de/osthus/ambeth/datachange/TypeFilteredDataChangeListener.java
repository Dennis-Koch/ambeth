package de.osthus.ambeth.datachange;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListener;

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
