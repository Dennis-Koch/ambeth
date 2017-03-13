package com.koch.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@XmlType(name = "Compound", namespace = "Comtrack")
public class Compound
{
	@SuppressWarnings("unused")
	@LogInstance(Compound.class)
	private ILogger log;

}
