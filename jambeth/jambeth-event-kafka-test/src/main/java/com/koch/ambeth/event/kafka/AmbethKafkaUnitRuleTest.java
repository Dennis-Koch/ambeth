package com.koch.ambeth.event.kafka;

/*-
 * #%L
 * jambeth-event-kafka-test
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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.apache.kafka.common.security.JaasUtils;
import org.junit.Rule;
import org.junit.Test;

import kafka.admin.TopicCommand;
import kafka.producer.KeyedMessage;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import kafka.utils.ZkUtils;

/**
 * This unit test class, tests the functionality of junit rule by sending/receiving a test topic to
 * make sure local zooper and kafka are created and working fine.
 */
public class AmbethKafkaUnitRuleTest {
	@Rule
	public AmbethKafkaJunitRule kafkaUnitRule = new AmbethKafkaJunitRule();

	@Test
	public void sendRcvMessage() throws Throwable {
		String testTopic = "TestTopic";
		createTestTopic(testTopic);
		KeyedMessage<String, String> keyedMessage =
				new KeyedMessage<>(testTopic, "key", "value");
		kafkaUnitRule.sendMessages(null, keyedMessage);
		System.out.println("****************************Message Sent!!!!!! = " + testTopic);
		List<String> messages = kafkaUnitRule.readMessages(testTopic, 1,
				new StringDecoder(new VerifiableProperties(new Properties())));
		assertEquals("value", messages.get(0));
		System.out.println("****************************Message Received!!!!!!  = " + testTopic);
	}

	/**
	 * This method is just added to create a test topic that can be used to test the fucntionlaity of
	 * this unit rule.
	 *
	 * @param topicName string name of the topic
	 */
	public void createTestTopic(String topicName) {
		createTestTopic(topicName, 1);
	}

	/**
	 * Create a test topic from outside the class to test the send/rcv functionality of unit rule
	 *
	 * @param topicName name of the topic
	 * @param numPartitions number of partitions in the topic
	 */
	public void createTestTopic(String topicName, Integer numPartitions) {
		// setup
		String[] arguments = new String[9];
		arguments[0] = "--create";
		arguments[1] = "--zookeeper";
		arguments[2] = kafkaUnitRule.zookeeperConnectionString();
		arguments[3] = "--replication-factor";
		arguments[4] = "1";
		arguments[5] = "--partitions";
		arguments[6] = "" + Integer.valueOf(numPartitions);
		arguments[7] = "--topic";
		arguments[8] = topicName;
		TopicCommand.TopicCommandOptions opts = new TopicCommand.TopicCommandOptions(arguments);

		ZkUtils zkUtils = ZkUtils.apply(opts.options().valueOf(opts.zkConnectOpt()), 30000, 30000,
				JaasUtils.isZkSecurityEnabled());

		// run
		TopicCommand.createTopic(zkUtils, opts);
	}
}
