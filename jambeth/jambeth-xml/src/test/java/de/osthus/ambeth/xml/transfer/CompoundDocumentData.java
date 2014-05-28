package de.osthus.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

@XmlType(name = "CompoundDocumentData", namespace = "Comtrack")
public class CompoundDocumentData
{
	@SuppressWarnings("unused")
	@LogInstance(CompoundDocumentData.class)
	private ILogger log;

}
