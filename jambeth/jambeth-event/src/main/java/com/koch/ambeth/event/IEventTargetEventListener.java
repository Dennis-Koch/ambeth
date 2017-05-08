package com.koch.ambeth.event;

/*-
 * #%L
 * jambeth-event
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

import java.util.List;

/**
 * A more complex version of the {@link IEventListener}. Here the implementor gets additional access
 * to eventTargets which are needed for more complex synchronization scenarios. One of its usecases
 * is in the {@link com.koch.ambeth.cache.datachange.CacheDataChangeListener} where all
 * corresponding cache instances have to be "reserved" before the DataChangeEvent should be
 * processed.<br>
 * <br>
 *
 * The concept of "eventTargets" here connects with the {@link IEventQueue#pause(Object)} and
 * {@link IEventQueue#resume(Object)} methods. In the mentioned example above all Cache instances
 * use these methods to propagate that they are "under use" by application code when necessary.
 */
public interface IEventTargetEventListener extends IEventListenerMarker {
	void handleEvent(Object eventObject, Object resumedEventTarget, List<Object> pausedEventTargets,
			long dispatchTime, long sequenceId) throws Exception;
}
