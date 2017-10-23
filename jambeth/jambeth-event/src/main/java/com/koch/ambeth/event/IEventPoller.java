package com.koch.ambeth.event;

import java.beans.Introspector;

import com.koch.ambeth.util.model.INotifyPropertyChanged;

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

public interface IEventPoller extends INotifyPropertyChanged {
	String P_ACTIVE = Introspector.decapitalize("Active");

	String P_CONNECTED = Introspector.decapitalize("Connected");

	boolean isActive();

	boolean isConnected();

	void pausePolling();

	void resumePolling();
}
