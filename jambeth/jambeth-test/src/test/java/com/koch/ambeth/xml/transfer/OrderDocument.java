package com.koch.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@XmlType(name = "OrderDocument", namespace = "Comtrack")
public class OrderDocument
{
	@SuppressWarnings("unused")
	@LogInstance(OrderDocument.class)
	private ILogger log;

}
