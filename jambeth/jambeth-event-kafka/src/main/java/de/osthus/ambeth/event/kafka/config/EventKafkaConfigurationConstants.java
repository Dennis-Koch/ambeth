package de.osthus.ambeth.event.kafka.config;

import de.osthus.ambeth.annotation.ConfigurationConstantDescription;
import de.osthus.ambeth.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class EventKafkaConfigurationConstants
{
	@ConfigurationConstantDescription("Kafka Topic to publish/subscribe for Ambeth Events")
	public static final String TOPIC_NAME = "ambeth.event.kafka.topic";
}
