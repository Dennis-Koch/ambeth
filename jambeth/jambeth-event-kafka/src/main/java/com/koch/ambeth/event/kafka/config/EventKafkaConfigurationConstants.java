package com.koch.ambeth.event.kafka.config;

/*-
 * #%L
 * jambeth-event-kafka
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
public final class EventKafkaConfigurationConstants
{
	@ConfigurationConstantDescription("Kafka Topic to publish/subscribe for Ambeth Events")
	public static final String TOPIC_NAME = "ambeth.event.kafka.topic";
}
