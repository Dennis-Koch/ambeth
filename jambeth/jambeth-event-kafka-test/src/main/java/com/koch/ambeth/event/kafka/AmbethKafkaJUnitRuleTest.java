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

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This unit test class, tests the functionality of junit rule by sending/receiving a test topic to
 * make sure local zookeeper and kafka are created and working fine.
 */
@ExtendWith(AmbethKafkaJUnitRule.class)
public class AmbethKafkaJUnitRuleTest {
    AmbethKafkaJUnitRule kafkaRule;

    @Test
    public void sendRcvMessage() {
        var topicName = kafkaRule.createTopic("TestTopic", 1);
        kafkaRule.sendMessages(topicName, StringSerializer.class.getName(), "value");
        var messages = kafkaRule.readMessages(topicName, 1, StringDeserializer.class.getName());
        assertThat(messages.get(0)).isEqualTo("value");
    }
}
