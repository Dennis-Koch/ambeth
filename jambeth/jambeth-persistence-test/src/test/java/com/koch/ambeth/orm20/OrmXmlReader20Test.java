package com.koch.ambeth.orm20;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.w3c.dom.Document;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.orm.EntityConfig;
import com.koch.ambeth.merge.orm.IOrmEntityTypeProvider;
import com.koch.ambeth.merge.orm.OrmXmlReader20;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.xml.IXmlConfigUtil;

@TestModule(OrmXmlReader20TestModule.class)
public class OrmXmlReader20Test extends AbstractIocTest
{
	protected static final IDatabase database = new DatabaseDummy();

	@Autowired
	protected IOrmEntityTypeProvider defaultOrmEntityTypeProvider;

	@Autowired
	protected OrmXmlReader20 reader;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Test
	public void testLoadFromDocument()
	{
		String xmlFileNames = "com/koch/ambeth/orm20/orm20.xml";
		Document[] docs = xmlConfigUtil.readXmlFiles(xmlFileNames);
		assertEquals(1, docs.length);

		Set<EntityConfig> localEntities = new HashSet<EntityConfig>();
		Set<EntityConfig> externalEntities = new HashSet<EntityConfig>();
		reader.loadFromDocument(docs[0], localEntities, externalEntities, defaultOrmEntityTypeProvider);

		assertEquals(4, localEntities.size());
		assertEquals(0, externalEntities.size());
	}

	@Test
	public void testLoadFromDocumentIndependent()
	{
		String xmlFileNames = "com/koch/ambeth/orm20/independent-orm20.xml";
		Document[] docs = xmlConfigUtil.readXmlFiles(xmlFileNames);
		assertEquals(1, docs.length);

		Set<EntityConfig> localEntities = new HashSet<EntityConfig>();
		Set<EntityConfig> externalEntities = new HashSet<EntityConfig>();
		reader.loadFromDocument(docs[0], localEntities, externalEntities, defaultOrmEntityTypeProvider);

		assertEquals(4, localEntities.size());
		assertEquals(2, externalEntities.size());
	}
}
