package de.osthus.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@XmlType(name = "Compound", namespace = "Comtrack")
public class Compound
{
	@SuppressWarnings("unused")
	@LogInstance(Compound.class)
	private ILogger log;

}
