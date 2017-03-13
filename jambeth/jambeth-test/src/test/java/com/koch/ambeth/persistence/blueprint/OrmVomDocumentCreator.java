package com.koch.ambeth.persistence.blueprint;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.xml.XmlConstants;

public class OrmVomDocumentCreator
		implements IInitializingBean, IVomDocumentCreator, IOrmDocumentCreator {
	protected static final String VOM_NAMESPACE = "http://schema.kochdev.com/ambeth/ambeth_vom_2_0";

	protected static final String ORM_NAMESPACE = "http://schema.kochdev.com/ambeth/ambeth_orm_2_0";

	@LogInstance
	private ILogger log;

	protected DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	@Override
	public Document getOrmDocument(String businessObjectType, String table) {

		Document doc = getDocument();
		Element ormElement = doc.createElementNS(ORM_NAMESPACE, "or-mappings");
		doc.appendChild(ormElement);
		Element entitiesMappingElement =
				doc.createElementNS(ORM_NAMESPACE, XmlConstants.ENTITY_MAPPINGS);
		ormElement.appendChild(entitiesMappingElement);

		Element entityElement;
		if (table == null) {
			entityElement = doc.createElementNS(ORM_NAMESPACE, XmlConstants.EXTERNAL_ENTITY);
		}
		else {
			entityElement = doc.createElementNS(ORM_NAMESPACE, XmlConstants.ENTITY);
			Element tableElement = doc.createElementNS(ORM_NAMESPACE, XmlConstants.TABLE);
			entityElement.setAttribute(XmlConstants.NAME, table);
			entityElement.appendChild(tableElement);
		}
		// TODO: Enhance with property name mapping (property names in DB can differ from name in entity
		// due to length)
		entitiesMappingElement.appendChild(entityElement);
		entityElement.setAttribute(XmlConstants.CLASS, businessObjectType);

		return doc;

	}

	@Override
	public Document getVomDocument(String businessObjectType, String valueObjectType) {
		Document doc = getDocument();
		Element vomElement = doc.createElementNS(VOM_NAMESPACE, "value-object-mappings");
		doc.appendChild(vomElement);
		Element entityElement = doc.createElementNS(VOM_NAMESPACE, XmlConstants.ENTITY);
		vomElement.appendChild(entityElement);
		entityElement.setAttribute(XmlConstants.CLASS, businessObjectType);

		Element voElement = doc.createElementNS(VOM_NAMESPACE, XmlConstants.VALUE_OBJECT);
		entityElement.appendChild(voElement);
		voElement.setAttribute(XmlConstants.CLASS, valueObjectType);

		return doc;

	}

	protected Document getDocument() {
		try {
			return dbf.newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Throwable {
		dbf.setNamespaceAware(true);
	}
}
