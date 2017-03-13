package com.koch.ambeth.datachange;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;

public class UnfilteredDataChangeListener implements IEventListener, IDataChangeListener, IInitializingBean, IDisposableBean
{
	public static IDataChangeListener create(IDataChangeListener dataChangeListener)
	{
		UnfilteredDataChangeListener dcListener = new UnfilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		return dcListener;
	}

	public static IEventListener createEventListener(IDataChangeListener dataChangeListener)
	{
		UnfilteredDataChangeListener dcListener = new UnfilteredDataChangeListener();
		dcListener.dataChangeListener = dataChangeListener;
		return dcListener;
	}

	protected IDataChangeListener dataChangeListener;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(dataChangeListener, "DataChangeListener");
	}

	public void setDataChangeListener(IDataChangeListener dataChangeListener)
	{
		this.dataChangeListener = dataChangeListener;
	}

	@Override
	public void destroy() throws Throwable
	{
		dataChangeListener = null;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (!(eventObject instanceof IDataChange))
		{
			return;
		}
		dataChanged((IDataChange) eventObject, dispatchTime, sequenceId);
	}

	@Override
	public void dataChanged(IDataChange dataChange, long dispatchTime, long sequenceId)
	{
		if (dataChange.isEmpty())
		{
			return;
		}
		dataChangeListener.dataChanged(dataChange, dispatchTime, sequenceId);
	}
}
