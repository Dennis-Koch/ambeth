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
import kafka.producer.ProducerConfig;
import kafka.serializer.Decoder;
import kafka.serializer.DefaultDecoder;
import kafka.serializer.StringDecoder;
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

public class AmbethKafkaJunitRule extends ExternalResource
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaJunitRule.class);
	private static final int ALLOCATE_RANDOM_PORT = -1;
	private static final String LOCALHOST = "localhost";

	private TestingServer zookeeper;
	private KafkaServerStartable kafkaServer;

	private int zookeeperPort = ALLOCATE_RANDOM_PORT;
	private String zookeeperConnectionString;
	private int kafkaPort;
	private Path kafkaLogDir;
	private Properties props;
	private String testName;
	private Path zookeeperLogDir;
	private Path tempTestDir;

	@Override
	public Statement apply(Statement base, Description description)
	{
		testName = description.getTestClass().getName() + '.' + description.getMethodName();
		de.osthus.ambeth.config.Properties props = new de.osthus.ambeth.config.Properties();
		AmbethIocRunner.extendProperties(description.getTestClass(), null, props);
		this.props = AmbethZookeeperConfiguration.extractZookeeperProperties(props);

		Object zookeeperPort = this.props.get(AmbethZookeeperConfiguration.CLIENT_PORT);
		if (zookeeperPort == null)
		{
			this.zookeeperPort = InstanceSpec.getRandomPort();
		}
		else
		{
			this.zookeeperPort = Integer.parseInt(zookeeperPort.toString());
		}
		Object kafkaPort = this.props.get(AmbethKafkaConfiguration.KAFKA_PORT);
		if (kafkaPort == null)
		{
			this.kafkaPort = InstanceSpec.getRandomPort();
		}
		else
		{
			this.kafkaPort = Integer.parseInt(kafkaPort.toString());
		}
		return super.apply(base, description);
	}

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
	protected void before() throws Throwable
	{
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
		}
		{
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
		zookeeper = new TestingServer(zookeeperPort, zookeeperLogDir.toFile(), true);
		zookeeperConnectionString = zookeeper.getConnectString();

		KafkaConfig kafkaConfig = buildKafkaConfig(zookeeperConnectionString);

		LOGGER.info("Starting Kafka server with config: {}", kafkaConfig.props());
		kafkaServer = new KafkaServerStartable(kafkaConfig);
		startKafka();
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
	 * 
	 * Shutdown Kafka Broker before the test termination to test consumer exceptions
	 * 
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
		props.put("zookeeper.connect", zookeeperConnectionString);
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
		return zookeeperConnectionString;
	}
}