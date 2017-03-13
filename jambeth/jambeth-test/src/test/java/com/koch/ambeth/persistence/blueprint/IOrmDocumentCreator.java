package com.koch.ambeth.persistence.blueprint;

import org.w3c.dom.Document;

public interface IOrmDocumentCreator
{

	Document getOrmDocument(String businessObjectType, String table);

}