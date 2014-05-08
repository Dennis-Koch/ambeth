package de.osthus.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@XmlType(name = "OrderDocument", namespace = "Comtrack")
public class OrderDocument
{
	@SuppressWarnings("unused")
	@LogInstance(OrderDocument.class)
	private ILogger log;

}
