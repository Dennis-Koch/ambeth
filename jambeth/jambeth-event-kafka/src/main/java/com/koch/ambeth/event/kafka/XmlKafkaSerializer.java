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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.ioc.XmlModule;

public class XmlKafkaSerializer implements Serializer<Object>, Deserializer<Object>
{
	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Override
	public void configure(Map<String, ?> configs, boolean isKey)
	{
	}

	@Override
	public void close()
	{
	}

	@Override
	public byte[] serialize(String topic, Object data)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		cyclicXmlHandler.writeToStream(bos, data);
		return bos.toByteArray();
	}

	@Override
	public Object deserialize(String topic, byte[] data)
	{
		return cyclicXmlHandler.readFromStream(new ByteArrayInputStream(data));
	}
}
