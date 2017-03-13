package com.koch.ambeth.datachange.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IProcessResumeItem;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class FilterRegistry implements IFilterExtendable, IEventDispatcher
{
	private static final String[] EMPTY_STRINGS = new String[0];

	@LogInstance
	private ILogger log;

	protected HashMap<String, IFilter> topicToFilterDict = new HashMap<String, IFilter>();

	protected IEventDispatcher eventDispatcher;

	public void setEventDispatcher(IEventDispatcher eventDispatcher)
	{
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public boolean isDispatchingBatchedEvents()
	{
		return false;
	}

	@Override
	public void enableEventQueue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void flushEventQueue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void dispatchEvent(Object eventObject)
	{
		dispatchEvent(eventObject, System.currentTimeMillis(), -1);
	}

	@Override
	public void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (eventObject instanceof IDataChange)
		{
			IDataChange dataChange = (IDataChange) eventObject;
			synchronized (topicToFilterDict)
			{
				// lock (topicToFilterDict)
				// TODO discuss
				try
				{
					Iterable<Map.Entry<String, IFilter>> dictIter = topicToFilterDict;

					for (IDataChangeEntry dataChangeEntry : dataChange.getAll())
					{
						String[] topics = evaluateMatchingTopics(dataChangeEntry, dictIter);
						dataChangeEntry.setTopics(topics);
					}
				}
				catch (Exception e)
				{
				}

			}
		}
		eventDispatcher.dispatchEvent(eventObject, dispatchTime, sequenceId);
	}

	@Override
	public boolean hasListeners(Class<?> eventType)
	{
		return eventDispatcher.hasListeners(eventType);
	}

	@Override
	public void waitEventToResume(Object eventTargetToResume, long maxWaitTime, IBackgroundWorkerParamDelegate<IProcessResumeItem> resumeDelegate,
			IBackgroundWorkerParamDelegate<Throwable> errorDelegate)
	{
		try
		{
			resumeDelegate.invoke(null);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected String[] evaluateMatchingTopics(final IDataChangeEntry dataChangeEntry, Iterable<Map.Entry<String, IFilter>> dictIter)
	{
		final List<String> topics = new ArrayList<String>();
		for (Entry<String, IFilter> entry : dictIter)
		{
			String topic = entry.getKey();
			IFilter filter = entry.getValue();
			try
			{
				if (filter.doesFilterMatch(dataChangeEntry))
				{
					topics.add(topic);
				}
			}
			catch (Exception e)
			{
				if (log.isErrorEnabled())
				{
					log.error("Error while handling filter '" + filter + "' on topic '" + topic + "'. Skipping this filter", e);
				}
			}
		}
		if (topics.size() == 0)
		{
			return EMPTY_STRINGS;
		}
		return topics.toArray(new String[topics.size()]);
	}

	@Override
	public void registerFilter(IFilter filter, String topic)
	{
		if (filter == null)
		{
			throw new IllegalArgumentException("Argument must not be null: filter");
		}
		if (topic == null)
		{
			throw new IllegalArgumentException("Argument must not be null: topic");
		}
		topic = topic.trim();
		if (topic.length() == 0)
		{
			throw new IllegalArgumentException("Argument must be valid: topic");
		}
		synchronized (topicToFilterDict)
		{
			if (topicToFilterDict.containsKey(topic))
			{
				throw new IllegalArgumentException("Given topic already registered with a filter");
			}
			topicToFilterDict.put(topic, filter);
		}
	}

	@Override
	public void unregisterFilter(IFilter filter, String topic)
	{
		if (filter == null)
		{
			throw new IllegalArgumentException("Argument must not be null: filter");
		}
		if (topic == null)
		{
			throw new IllegalArgumentException("Argument must not be null: topic");
		}
		topic = topic.trim();
		if (topic.length() == 0)
		{
			throw new IllegalArgumentException("Argument must be valid: topic");
		}
		synchronized (topicToFilterDict)
		{
			IFilter registeredFilter = topicToFilterDict.get(topic);
			if (!(registeredFilter == filter))
			{
				throw new IllegalArgumentException("Given topic is registered with another filter. Unregistering illegal");
			}
			topicToFilterDict.remove(topic);
		}
	}

	@Override
	public void pause(Object eventTarget)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void resume(Object eventTarget)
	{
		throw new UnsupportedOperationException();
	}
}
