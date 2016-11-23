package de.osthus.ambeth.kafka;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import kafka.admin.TopicCommand;
import kafka.producer.KeyedMessage;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import kafka.utils.ZkUtils;

import org.apache.kafka.common.security.JaasUtils;
import org.junit.Rule;
import org.junit.Test;

/**
 * This unit test class, tests the functionality of junit rule by sending/receiving a test topic to make sure local zooper and kafka are created and working
 * fine.
 */
public class AmbethKafkaUnitRuleTest
{
	@Rule
	public AmbethKafkaJunitRule kafkaUnitRule = new AmbethKafkaJunitRule();

	@Test
	public void sendRcvMessage() throws Throwable
	{
		String testTopic = "TestTopic";
		createTestTopic(testTopic);
		KeyedMessage<String, String> keyedMessage = new KeyedMessage<String, String>(testTopic, "key", "value");
		kafkaUnitRule.sendMessages(null, keyedMessage);
		System.out.println("****************************Message Sent!!!!!! = " + testTopic);
		List<String> messages = kafkaUnitRule.readMessages(testTopic, 1, new StringDecoder(new VerifiableProperties(new Properties())));
		assertEquals("value", messages.get(0));
		System.out.println("****************************Message Received!!!!!!  = " + testTopic);
	}

	/**
	 * This method is just added to create a test topic that can be used to test the fucntionlaity of this unit rule.
	 *
	 * @param topicName
	 *            string name of the topic
	 */
	public void createTestTopic(String topicName)
	{
		createTestTopic(topicName, 1);
	}

	/**
	 * Create a test topic from outside the class to test the send/rcv functionality of unit rule
	 *
	 * @param topicName
	 *            name of the topic
	 * @param numPartitions
	 *            number of partitions in the topic
	 */
	public void createTestTopic(String topicName, Integer numPartitions)
	{
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

		ZkUtils zkUtils = ZkUtils.apply(opts.options().valueOf(opts.zkConnectOpt()), 30000, 30000, JaasUtils.isZkSecurityEnabled());

		// run
		TopicCommand.createTopic(zkUtils, opts);
	}
}
