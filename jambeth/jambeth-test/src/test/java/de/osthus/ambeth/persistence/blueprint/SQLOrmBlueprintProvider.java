package de.osthus.ambeth.persistence.blueprint;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.orm.blueprint.IBlueprintOrmProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintVomProvider;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;

public class SQLOrmBlueprintProvider implements IBlueprintProvider, IInitializingBean, IBlueprintOrmProvider, IBlueprintVomProvider
{
	@Autowired
	protected ICache cache;
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		dbf.setNamespaceAware(true);
	}

	@Override
	public IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName)
	{
		return cache.getObject(IEntityTypeBlueprint.class, IEntityTypeBlueprint.NAME, entityTypeName);
	}

	@Override
	public Document[] getOrmDocuments()
	{
		try
		{
			Document document = dbf
					.newDocumentBuilder()
					.parse(new InputSource(
							new StringReader(
									"<?xml version=\"1.0\" encoding=\"UTF-8\"?><or-mappings xmlns=\"http://osthus.de/ambeth/ambeth_orm_2_0\"><entity-mappings><external-entity class=\"de.osthus.ambeth.persistence.blueprint.TestClass\"/></entity-mappings></or-mappings>")));
			return new Document[] { document };
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Document[] getVomDocuments()
	{
		try
		{
			Document document = dbf
					.newDocumentBuilder()
					.parse(new InputSource(
							new StringReader(
									"<?xml version=\"1.0\" encoding=\"UTF-8\"?><value-object-mappings xmlns=\"http://osthus.de/ambeth/ambeth_vom_2_0\"><entity class=\"de.osthus.ambeth.persistence.blueprint.TestClass\"><value-object class=\"de.osthus.ambeth.persistence.blueprint.TestClassV\"/></entity></value-object-mappings>")));
			return new Document[] { document };
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

}
