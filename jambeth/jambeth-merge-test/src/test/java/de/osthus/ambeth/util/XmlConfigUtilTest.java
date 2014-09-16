package de.osthus.ambeth.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.w3c.dom.Document;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.util.XmlConfigUtilTest.XmlConfigUtilTestModule;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlValidator;

@TestModule(XmlConfigUtilTestModule.class)
public class XmlConfigUtilTest extends AbstractIocTest
{
	private static final String AMBETH_FOLDER = "de/osthus/ambeth/";

	private static final String AMBETH_SCHEMA_FOLDER = AMBETH_FOLDER + "schema/";

	private static final String XSD_SIMPLE_TYPES_2_0 = AMBETH_SCHEMA_FOLDER + "ambeth_simple_types_2_0.xsd";

	private static final String XSD_ORM_2_0 = AMBETH_SCHEMA_FOLDER + "ambeth_orm_2_0.xsd";

	private static final String ORM_XML_SIMPLE = AMBETH_FOLDER + "util/orm_simple.xml";

	public static class XmlConfigUtilTestModule implements IInitializingModule
	{

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class).autowireable(IXmlConfigUtil.class);
		}

	}

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Test
	public void testReadXmlFiles()
	{
		Document[] docs = xmlConfigUtil.readXmlFiles(ORM_XML_SIMPLE);
		assertNotNull(docs);
		assertEquals(1, docs.length);
	}

	@Test
	public void testCreateValidator() throws Exception
	{
		IXmlValidator validator1 = xmlConfigUtil.createValidator(XSD_SIMPLE_TYPES_2_0);
		assertNotNull(validator1);

		IXmlValidator validator2 = xmlConfigUtil.createValidator(XSD_SIMPLE_TYPES_2_0, XSD_ORM_2_0);
		assertNotNull(validator2);

		Document doc = xmlConfigUtil.readXmlFiles(ORM_XML_SIMPLE)[0];
		validator2.validate(doc);
	}
}
