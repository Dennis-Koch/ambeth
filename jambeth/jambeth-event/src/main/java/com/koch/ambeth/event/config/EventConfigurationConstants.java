package com.koch.ambeth.event.config;

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

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class EventConfigurationConstants {
	private EventConfigurationConstants() {
	}

	@ConfigurationConstantDescription("TODO")
	public static final String PollingActive = "event.polling.active";

	@ConfigurationConstantDescription("TODO")
	public static final String StartPausedActive = "event.polling.paused.on.start.active";

	@ConfigurationConstantDescription("TODO")
	public static final String PollingSleepInterval = "event.polling.sleepinterval";

	@ConfigurationConstantDescription("TODO")
	public static final String MaxWaitInterval = "event.polling.maxwaitinterval";

	@ConfigurationConstantDescription("TODO")
	public static final String EventManagerName = "event.manager.name";

}
