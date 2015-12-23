package de.osthus.ambeth.persistence.blueprint;

import org.w3c.dom.Document;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public interface IVomDocumentCreator
{

	Document getVomDocument(String businessObjectType, String valueObjectType);

}