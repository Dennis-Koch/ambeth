package de.osthus.ambeth.event.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class EventConfigurationConstants
{
	private EventConfigurationConstants()
	{
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
