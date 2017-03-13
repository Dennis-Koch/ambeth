package com.koch.ambeth.datachange.kafka;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public interface TestEntity
{
	int getId();

	int getVersion();
}