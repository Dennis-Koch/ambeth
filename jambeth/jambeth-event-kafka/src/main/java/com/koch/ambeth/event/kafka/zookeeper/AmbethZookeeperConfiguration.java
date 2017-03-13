package com.koch.ambeth.event.kafka.zookeeper;

import java.util.Properties;

import com.koch.ambeth.util.config.IProperties;

public final class AmbethZookeeperConfiguration
{
	public static final String AMBETH_ZOOKEEPER_PROP_PREFIX = "ambeth.zookeeper.";

	public static final String DATA_DIR = "dataDir";

	public static final String CLIENT_PORT = "clientPort";

	public static Properties extractZookeeperProperties(IProperties props)
	{
		Properties kafkaProps = new Properties();
		for (String key : props.collectAllPropertyKeys())
		{
			if (!key.startsWith(AMBETH_ZOOKEEPER_PROP_PREFIX))
			{
				continue;
			}
			String kafkaKey = key.substring(AMBETH_ZOOKEEPER_PROP_PREFIX.length());
			kafkaProps.put(kafkaKey, props.get(key));
		}
		return kafkaProps;
	}

	private AmbethZookeeperConfiguration()
	{
		// intended blank
	}
}
