package de.osthus.ambeth.datachange;

import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.ParamChecker;

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
