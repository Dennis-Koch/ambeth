package com.koch.ambeth.xml.transfer;

import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

@XmlType(name = "CompoundDocumentData", namespace = "Comtrack")
public class CompoundDocumentData
{
	@SuppressWarnings("unused")
	@LogInstance(CompoundDocumentData.class)
	private ILogger log;

}
