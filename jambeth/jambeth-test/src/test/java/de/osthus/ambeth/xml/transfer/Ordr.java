package de.osthus.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@XmlType(name = "Ordr", namespace = "Comtrack")
public class Ordr
{
	@SuppressWarnings("unused")
	@LogInstance(Ordr.class)
	private ILogger log;

}
