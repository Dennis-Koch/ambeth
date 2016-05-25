package de.osthus.ambeth.event.kafka;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.xml.ICyclicXMLHandler;

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
