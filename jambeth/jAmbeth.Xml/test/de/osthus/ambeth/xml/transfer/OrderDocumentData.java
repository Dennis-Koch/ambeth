package de.osthus.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@XmlType(name = "OrderDocumentData", namespace = "Comtrack")
public class OrderDocumentData
{
	@SuppressWarnings("unused")
	@LogInstance(OrderDocumentData.class)
	private ILogger log;

}
