package com.koch.ambeth.persistence.blueprint;

import org.w3c.dom.Document;

public interface IVomDocumentCreator
{

	Document getVomDocument(String businessObjectType, String valueObjectType);

}