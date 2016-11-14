package de.osthus.ambeth.kafka;

import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.serializer.Decoder;
import kafka.serializer.DefaultDecoder;
import kafka.serializer.StringDecoder;
import kafka.serializer.StringEncoder;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.charithe.kafka.KafkaJunitRule;
import com.yammer.metrics.Metrics;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.testutil.AmbethIocRunner;
import de.osthus.ambeth.zookeeper.AmbethZookeeperConfiguration;

/**
 * Unit rule class to assist the required operations related to kafka and zookeeper configurations. It is responsible for starting and closing kafka, and
 * zookeper and facilitates the mechanism for connecting with kafka server created at runtime.
 */
public class AmbethKafkaJunitRule extends ExternalResource
{
	// constant variables
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaJunitRule.class);
	private static final int ALLOCATE_RANDOM_PORT = -1;
	private static final String LOCALHOST = "localhost";

	// Object variables for zookeeper and kafka server instances
	protected TestingServer zookeeper;
	protected KafkaServerStartable kafkaServer;

	// variables for zookeeper and kafka port
	protected int zookeeperPort = ALLOCATE_RANDOM_PORT;
	protected int kafkaPort;

	// test name string
	protected String testName;

	// configuration properties
	protected Properties props;

	// log file directories
	protected Path kafkaLogDir;
	protected Path zookeeperLogDir;
	private Path tempTestDir;

	/**
	 * In this method perform the following operations before returning a statement: Extract the defined properties, Allocate random ports, start zookeeper and
	 * kafka server.
	 */
	@Override
	public Statement apply(Statement base, Description description)
	{
		// set test name, fetch and allocate respective properties
		testName = description.getTestClass().getName() + '.' + description.getMethodName();
		de.osthus.ambeth.config.Properties props = new de.osthus.ambeth.config.Properties();
		AmbethIocRunner.extendProperties(description.getTestClass(), null, props);
		this.props = AmbethZookeeperConfiguration.extractZookeeperProperties(props);

		// allocate ports for kafka and zookeeper
		allocatePorts();
		// start kafka and zookeeper
		startZookeeperAndKafka();

		return super.apply(base, description);
	}

	/**
	 * This method is responsible for allocating ports to kafka and zookeeper. These ports will later be used to establish the connection with respective
	 * entities.
	 */
	protected void allocatePorts()
	{
		// try to get zookeeper port from properties else get a random port
		Object zookeeperPort = props.get(AmbethZookeeperConfiguration.CLIENT_PORT);
		if (zookeeperPort == null)
		{
			this.zookeeperPort = InstanceSpec.getRandomPort();
		}
		else
		{
			this.zookeeperPort = Integer.parseInt(zookeeperPort.toString());
		}

		// try to get kafka port from properties else get a random port
		Object kafkaPort = props.get(AmbethKafkaConfiguration.KAFKA_PORT);
		if (kafkaPort == null)
		{
			this.kafkaPort = InstanceSpec.getRandomPort();
		}
		else
		{
			this.kafkaPort = Integer.parseInt(kafkaPort.toString());
		}
	}

	/**
	 * Method is responsible for creating the required log files and starting zookeeper and kafka.
	 */
	protected void startZookeeperAndKafka()
	{
		// create respective log files
		allocateLogFilePaths();

		// create respective instances and start zookeeper and kafka server
		try
		{
			zookeeper = new TestingServer(zookeeperPort, zookeeperLogDir.toFile(), true);
			KafkaConfig kafkaConfig = buildKafkaConfig(zookeeper.getConnectString());

			// LOGGER.info("Starting Kafka server with config: {}", kafkaConfig.props());
			kafkaServer = new KafkaServerStartable(kafkaConfig);
			startKafka();
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * This method creates temporary directories if required and allocates the log file paths to respective zookeeper and kafka.
	 */
	protected void allocateLogFilePaths()
	{
		Object zookeeperLogDir = props.get(AmbethZookeeperConfiguration.DATA_DIR);
		if (zookeeperLogDir == null)
		{
			this.zookeeperLogDir = ensureTempTestDir().resolve("zookeeper");
		}
		else
		{
			this.zookeeperLogDir = Paths.get(zookeeperLogDir.toString()).toAbsolutePath().normalize();
		}

		Object kafkaLogDir = props.get(AmbethKafkaConfiguration.LOG_DIRECTORY);
		if (kafkaLogDir == null)
		{
			this.kafkaLogDir = ensureTempTestDir().resolve("kafka");
		}
		else
		{
			this.kafkaLogDir = Paths.get(kafkaLogDir.toString()).toAbsolutePath().normalize();
		}
	}

	/**
	 * Create a temporary directory
	 *
	 * @return directory path
	 */
	protected Path ensureTempTestDir()
	{
		if (tempTestDir != null)
		{
			return tempTestDir;
		}
		Path currDir = Paths.get(".").toAbsolutePath().normalize();
		Path tempDir = currDir.resolve("temp");
		try
		{
			deleteRecursive(tempDir);
			Files.createDirectories(tempDir);
			tempTestDir = Files.createTempDirectory(tempDir, testName);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		return tempTestDir;
	}

	@Override
	protected void after()
	{
		try
		{
			shutdownKafka();

			if (zookeeper != null)
			{
				LOGGER.info("Shutting down Zookeeper");
				zookeeper.close();
			}
			// fixes leaking "metrics-core" threadPool threads
			Metrics.shutdown();
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					cleanup();
				}
			}));
			cleanup();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to clean-up Kafka", e);
		}
	}

	/**
	 * Delete the created directories and clean up the required resources
	 */
	public void cleanup()
	{
		deleteRecursive(tempTestDir);
		deleteRecursive(kafkaLogDir);
		deleteRecursive(zookeeperLogDir);
	}

	private void deleteRecursive(Path path)
	{
		if (path == null || !Files.exists(path))
		{
			return;
		}
		LOGGER.info("Deleting the log dir:  {}", path);
		try
		{
			Files.walkFileTree(path, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					Files.deleteIfExists(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
				{
					Files.deleteIfExists(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * Starts the server
	 */
	public void startKafka()
	{
		if (kafkaServer != null)
		{
			LOGGER.info("Starting Kafka Server");
			kafkaServer.startup();
		}
	}

	/**
	 * Shutdown Kafka Broker before the test termination to test consumer exceptions
	 */
	public void shutdownKafka()
	{
		if (kafkaServer != null)
		{
			LOGGER.info("Shutting down Kafka Server");
			kafkaServer.shutdown();
		}
	}

	/**
	 * Build Kafka configuration object
	 *
	 * @param zookeeperQuorum
	 *            connection string
	 * @return Kafka Configuraion object
	 * @throws IOException
	 */
	private KafkaConfig buildKafkaConfig(String zookeeperQuorum) throws IOException
	{
		Properties props = new Properties();
		props.put(AmbethKafkaConfiguration.VISIBLE_HOST_NAME, LOCALHOST);
		props.put(AmbethKafkaConfiguration.KAFKA_PORT, kafkaPort + "");
		props.put(AmbethKafkaConfiguration.BROKER_ID, "1");
		props.put(AmbethKafkaConfiguration.LOG_DIRECTORY, kafkaLogDir.toString());
		props.put(AmbethKafkaConfiguration.ZOOKEEPER_URL, zookeeperQuorum);

		return new KafkaConfig(props);
	}

	/**
	 * Create a producer configuration. Sets the serializer class to "DefaultEncoder" and producer type to "sync"
	 *
	 * @return {@link ProducerConfig}
	 */
	public ProducerConfig producerConfigWithDefaultEncoder()
	{
		return producerConfig("kafka.serializer.DefaultEncoder");
	}

	/**
	 * Create a producer configuration. Sets the serializer class to "StringEncoder" and producer type to "sync"
	 *
	 * @return {@link ProducerConfig}
	 */
	public ProducerConfig producerConfigWithStringEncoder()
	{
		return producerConfig("kafka.serializer.StringEncoder");
	}

	/**
	 * Create a producer configuration. Sets the serializer class to specified encoder and producer type to "sync"
	 *
	 * @return {@link ProducerConfig}
	 */
	public ProducerConfig producerConfig(String serializerClass)
	{
		Properties props = new Properties();
		props.put("external.bootstrap.servers", LOCALHOST + ":" + kafkaPort);
		props.put("metadata.broker.list", LOCALHOST + ":" + kafkaPort);
		props.put("serializer.class", serializerClass);
		props.put("producer.type", "sync");
		props.put("request.required.acks", "1");

		return new ProducerConfig(props);
	}

	/**
	 * Create a consumer configuration Offset is set to "smallest"
	 *
	 * @return {@link ConsumerConfig}
	 */
	public ConsumerConfig consumerConfig()
	{
		Properties props = new Properties();
		props.put("zookeeper.connect", zookeeper.getConnectString());
		props.put("group.id", "kafka-junit-consumer");
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.offset.reset", "smallest");
		return new ConsumerConfig(props);
	}

	/**
	 * Read messages from a given kafka topic as {@link String}.
	 *
	 * @param topic
	 *            name of the topic
	 * @param expectedMessages
	 *            number of messages to be read
	 * @return list of string messages
	 * @throws TimeoutException
	 *             if no messages are read after 5 seconds
	 */
	public List<String> readStringMessages(final String topic, final int expectedMessages) throws TimeoutException
	{
		return readMessages(topic, expectedMessages, new StringDecoder(null));
	}

	/**
	 * Read messages from a given kafka topic.
	 *
	 * @param topic
	 *            name of the topic
	 * @param expectedMessages
	 *            number of messages to be read
	 * @param decoder
	 *            message decoder
	 * @return list of decoded messages
	 * @throws TimeoutException
	 *             if no messages are read after 5 seconds
	 */
	public <T> List<T> readMessages(final String topic, final int expectedMessages, final Decoder<T> decoder) throws TimeoutException
	{
		ExecutorService singleThread = Executors.newSingleThreadExecutor();
		ConsumerConnector connector = null;
		try
		{
			connector = Consumer.createJavaConsumerConnector(consumerConfig());

			Map<String, List<KafkaStream<byte[], T>>> streams = connector.createMessageStreams(singletonMap(topic, 1), new DefaultDecoder(null), decoder);

			final KafkaStream<byte[], T> messageSteam = streams.get(topic).get(0);

			Future<List<T>> future = singleThread.submit(new Callable<List<T>>()
			{
				@Override
				public List<T> call() throws Exception
				{
					List<T> messages = new ArrayList<T>(expectedMessages);
					ConsumerIterator<byte[], T> iterator = messageSteam.iterator();
					while (messages.size() != expectedMessages && iterator.hasNext())
					{
						T message = iterator.next().message();
						LOGGER.debug("Received message: {}", message);
						messages.add(message);
					}
					return messages;
				}
			});

			return future.get(5, SECONDS);
		}
		catch (TimeoutException e)
		{
			throw new TimeoutException("Timed out waiting for messages");
		}
		catch (InterruptedException e)
		{
			throw new TimeoutException("Timed out waiting for messages");
		}
		catch (ExecutionException e)
		{
			throw new TimeoutException("Timed out waiting for messages");
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected exception while reading messages", e);
		}
		finally
		{
			singleThread.shutdown();
			if (connector != null)
			{
				connector.shutdown();
			}
		}
	}

	/**
	 * Send messages to test unit rule functionlaity.
	 *
	 * @param producer
	 *            kafka producer
	 * @param message
	 *            string message
	 * @param messages
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final void sendMessages(Producer producer, KeyedMessage message, KeyedMessage... messages)
	{
		if (producer == null)
		{
			producer = new Producer<String, String>(producerConfig(StringEncoder.class.getName()));
		}
		producer.send(message);
		producer.send(Arrays.asList(messages));
	}

	/**
	 * Get the Kafka log directory
	 *
	 * @return kafka log directory path
	 */
	public Path kafkaLogDir()
	{
		return kafkaLogDir;
	}

	/**
	 * Get the kafka broker port
	 *
	 * @return broker port
	 */
	public int kafkaBrokerPort()
	{
		return kafkaPort;
	}

	/**
	 * Get the zookeeper port
	 *
	 * @return zookeeper port
	 */
	public int zookeeperPort()
	{
		return zookeeperPort;
	}

	/**
	 * Get the zookeeper connection string
	 *
	 * @return zookeeper connection string
	 */
	public String zookeeperConnectionString()
	{
		return zookeeper.getConnectString();
	}

}