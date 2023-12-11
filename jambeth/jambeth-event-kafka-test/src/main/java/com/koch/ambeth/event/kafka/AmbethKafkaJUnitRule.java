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

import com.koch.ambeth.testutil.AmbethIocRunner;
import com.koch.ambeth.util.config.UtilConfigurationConstants;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.burningwave.core.assembler.StaticComponentContainer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AmbethKafkaJUnitRule implements BeforeEachCallback {
    // constant variables
    private static final Logger LOGGER = LoggerFactory.getLogger(AmbethKafkaJUnitRule.class);

    static {
        StaticComponentContainer.Methods.toString();
    }

    // test name string
    protected String testName;

    protected String forkName;

    // configuration properties
    protected Properties kafkaProps;

    protected KafkaContainer kafkaContainer;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        var test = extensionContext.getTestInstance().get();
        for (var field : test.getClass().getDeclaredFields()) {
            if (AmbethKafkaJUnitRule.class.equals(field.getType())) {
                field.setAccessible(true);
                field.set(test, this);
            }
        }
        var testClass = extensionContext.getTestClass().get();
        var testMethod = extensionContext.getTestMethod().get();
        // set test name, fetch and allocate respective properties
        testName = testClass.getName() + '.' + testMethod.getName();
        var props = new com.koch.ambeth.log.config.Properties(com.koch.ambeth.log.config.Properties.getApplication());
        AmbethIocRunner.extendProperties(testClass, null, props);
        forkName = props.getString(UtilConfigurationConstants.ForkName);
        this.kafkaProps = AmbethKafkaConfiguration.extractKafkaProperties(props);

        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.3")).withKraft().withReuse(true).withEnv("KAFKA_LOG4J_ROOT_LOGLEVEL", "WARN");
        kafkaContainer.start();
    }

    public AdminClient adminClient() {
        var props = new Properties();
        props.put(AmbethKafkaConfiguration.BROKER_URL, kafkaContainer.getBootstrapServers());
        return AdminClient.create(props);
    }

    public ProducerConfig producerConfig(String serializerClass) {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getHost() + ":" + kafkaBrokerPort());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, serializerClass);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializerClass);
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        return new ProducerConfig(props);
    }

    public ConsumerConfig consumerConfig(String deserializerClass) {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getHost() + ":" + kafkaBrokerPort());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, deserializerClass);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-junit-consumer");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new ConsumerConfig(props);
    }

    public List<String> readStringMessages(String topic, int expectedMessages) throws TimeoutException {
        return readMessages(topic, expectedMessages, StringDeserializer.class.getName());
    }

    public <T> List<T> readMessages(String topic, int expectedMessages, String deserializerClass) throws TimeoutException {
        var consumer = new KafkaConsumer<String, T>(consumerConfig(deserializerClass).originals());
        var cancelled = new AtomicBoolean();
        try {
            consumer.subscribe(List.of(topic));

            var messages = new ArrayList<T>(expectedMessages);

            while (!cancelled.get() && messages.size() < expectedMessages) {
                var records = consumer.poll(Duration.ofMillis(100));
                if (records.isEmpty()) {
                    continue;
                }
                for (var record : records) {
                    messages.add(record.value());
                }
            }
            return messages;
        } finally {
            cancelled.set(true);
            consumer.close();
        }
    }

    public void sendMessages(String topic, String serializerClass, Object... messages) {
        try (var producer = new KafkaProducer<String, String>(producerConfig(serializerClass).originals())) {
            Arrays.stream(messages).map(message -> new ProducerRecord(topic, message)).forEach(producer::send);
            producer.flush();
        }
    }

    public void sendMessages(String topic, String serializerClass, Object message) {
        try (var producer = new KafkaProducer<String, String>(producerConfig(serializerClass).originals())) {
            producer.send(new ProducerRecord(topic, message));
            producer.flush();
        }
    }

    public int kafkaBrokerPort() {
        return kafkaContainer.getMappedPort(KafkaContainer.KAFKA_PORT);
    }

    public String kafkaBootstrapServers() {
        return kafkaContainer.getHost() + ":" + kafkaBrokerPort();
    }

    public int zookeeperPort() {
        return kafkaContainer.getMappedPort(KafkaContainer.ZOOKEEPER_PORT);
    }

    @SneakyThrows
    public String createTopic(String topicName, int numPartitions) {
        var adminClient = adminClient();
        if (forkName != null && !forkName.isEmpty()) {
            topicName = forkName + "-" + topicName;
        }
        adminClient.deleteTopics(List.of(topicName)).all().get(30, TimeUnit.SECONDS);
        adminClient.createTopics(List.of(new NewTopic(topicName, numPartitions, (short) 1))).all().get(30, TimeUnit.SECONDS);
        return topicName;
    }
}
