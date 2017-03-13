package com.koch.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@XmlType(name = "CompoundDocument", namespace = "Comtrack")
public class CompoundDocument
{
	@SuppressWarnings("unused")
	@LogInstance(CompoundDocument.class)
	private ILogger log;

}
