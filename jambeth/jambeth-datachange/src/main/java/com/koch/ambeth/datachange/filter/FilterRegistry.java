package com.koch.ambeth.datachange.filter;

/*-
 * #%L
 * jambeth-datachange
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IProcessResumeItem;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.state.IStateRollback;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FilterRegistry implements IFilterExtendable, IEventDispatcher {
    private static final String[] EMPTY_STRINGS = new String[0];
    protected HashMap<String, IFilter> topicToFilterDict = new HashMap<>();
    protected IEventDispatcher eventDispatcher;
    @LogInstance
    private ILogger log;

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean isDispatchingBatchedEvents() {
        return false;
    }

    @Override
    public IStateRollback enableEventQueue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushEventQueue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispatchEvent(Object eventObject) {
        dispatchEvent(eventObject, System.currentTimeMillis(), -1);
    }

    @Override
    public void dispatchEvent(Object eventObject, long dispatchTime, long sequenceId) {
        if (eventObject instanceof IDataChange) {
            IDataChange dataChange = (IDataChange) eventObject;
            synchronized (topicToFilterDict) {
                // lock (topicToFilterDict)
                // TODO discuss
                try {
                    Iterable<Map.Entry<String, IFilter>> dictIter = topicToFilterDict;

                    for (IDataChangeEntry dataChangeEntry : dataChange.getAll()) {
                        String[] topics = evaluateMatchingTopics(dataChangeEntry, dictIter);
                        dataChangeEntry.setTopics(topics);
                    }
                } catch (Exception e) {
                }

            }
        }
        eventDispatcher.dispatchEvent(eventObject, dispatchTime, sequenceId);
    }

    @Override
    public boolean hasListeners(Class<?> eventType) {
        return eventDispatcher.hasListeners(eventType);
    }

    @SneakyThrows
    @Override
    public void waitEventToResume(Object eventTargetToResume, long maxWaitTime, CheckedConsumer<IProcessResumeItem> resumeDelegate, CheckedConsumer<Throwable> errorDelegate) {
        resumeDelegate.accept(null);
    }

    protected String[] evaluateMatchingTopics(final IDataChangeEntry dataChangeEntry, Iterable<Map.Entry<String, IFilter>> dictIter) {
        final List<String> topics = new ArrayList<>();
        for (Entry<String, IFilter> entry : dictIter) {
            String topic = entry.getKey();
            IFilter filter = entry.getValue();
            try {
                if (filter.doesFilterMatch(dataChangeEntry)) {
                    topics.add(topic);
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error while handling filter '" + filter + "' on topic '" + topic + "'. Skipping this filter", e);
                }
            }
        }
        if (topics.isEmpty()) {
            return EMPTY_STRINGS;
        }
        return topics.toArray(new String[topics.size()]);
    }

    @Override
    public void registerFilter(IFilter filter, String topic) {
        if (filter == null) {
            throw new IllegalArgumentException("Argument must not be null: filter");
        }
        if (topic == null) {
            throw new IllegalArgumentException("Argument must not be null: topic");
        }
        topic = topic.trim();
        if (topic.length() == 0) {
            throw new IllegalArgumentException("Argument must be valid: topic");
        }
        synchronized (topicToFilterDict) {
            if (topicToFilterDict.containsKey(topic)) {
                throw new IllegalArgumentException("Given topic already registered with a filter");
            }
            topicToFilterDict.put(topic, filter);
        }
    }

    @Override
    public void unregisterFilter(IFilter filter, String topic) {
        if (filter == null) {
            throw new IllegalArgumentException("Argument must not be null: filter");
        }
        if (topic == null) {
            throw new IllegalArgumentException("Argument must not be null: topic");
        }
        topic = topic.trim();
        if (topic.length() == 0) {
            throw new IllegalArgumentException("Argument must be valid: topic");
        }
        synchronized (topicToFilterDict) {
            IFilter registeredFilter = topicToFilterDict.get(topic);
            if (!(registeredFilter == filter)) {
                throw new IllegalArgumentException("Given topic is registered with another filter. Unregistering illegal");
            }
            topicToFilterDict.remove(topic);
        }
    }

    @Override
    public IStateRollback pause(Object eventTarget) {
        throw new UnsupportedOperationException();
    }
}
