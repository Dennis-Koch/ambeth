package com.koch.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@XmlType(name = "OrderDocumentData", namespace = "Comtrack")
public class OrderDocumentData
{
	@SuppressWarnings("unused")
	@LogInstance(OrderDocumentData.class)
	private ILogger log;

}
