package com.koch.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@XmlType(name = "Ordr", namespace = "Comtrack")
public class Ordr
{
	@SuppressWarnings("unused")
	@LogInstance(Ordr.class)
	private ILogger log;

}
