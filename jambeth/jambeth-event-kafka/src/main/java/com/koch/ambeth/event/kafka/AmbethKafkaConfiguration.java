package com.koch.ambeth.event.kafka;

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

import java.util.Properties;

import com.koch.ambeth.util.config.IProperties;

public final class AmbethKafkaConfiguration {
	public static final String AMBETH_KAFKA_PROP_PREFIX = "ambeth.kafka.";

	public static final String BROKER_ID = "broker.id";

	public static final String BROKER_URL = "bootstrap.servers";

	public static final String LOG_DIRECTORY = "log.dirs";

	public static final String KAFKA_PORT = "port";

	public static final String VISIBLE_HOST_NAME = "advertised.host.name";

	public static final String ZOOKEEPER_URL = "zookeeper.connect";

	public static final String CONS_GROUP_ID = "group.id";

	public static Properties extractKafkaProperties(IProperties props) {
		Properties kafkaProps = new Properties();
		for (String key : props.collectAllPropertyKeys()) {
			if (!key.startsWith(AMBETH_KAFKA_PROP_PREFIX)) {
				continue;
			}
			String kafkaKey = key.substring(AMBETH_KAFKA_PROP_PREFIX.length());
			kafkaProps.put(kafkaKey, props.get(key));
		}
		return kafkaProps;
	}

	public static String buildAmbethProperty(String kafkaProperty) {
		return AMBETH_KAFKA_PROP_PREFIX + kafkaProperty;
	}

	private AmbethKafkaConfiguration() {
		// intended blank
	}
}
