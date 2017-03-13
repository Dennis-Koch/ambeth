package com.koch.ambeth.event.kafka.config;

import com.koch.ambeth.util.annotation.ConfigurationConstantDescription;
import com.koch.ambeth.util.annotation.ConfigurationConstants;

@ConfigurationConstants
public final class EventKafkaConfigurationConstants
{
	@ConfigurationConstantDescription("Kafka Topic to publish/subscribe for Ambeth Events")
	public static final String TOPIC_NAME = "ambeth.event.kafka.topic";
}
