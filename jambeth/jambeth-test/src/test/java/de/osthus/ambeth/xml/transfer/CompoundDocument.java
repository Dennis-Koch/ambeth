package de.osthus.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@XmlType(name = "CompoundDocument", namespace = "Comtrack")
public class CompoundDocument
{
	@SuppressWarnings("unused")
	@LogInstance(CompoundDocument.class)
	private ILogger log;

}
