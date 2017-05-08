package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-merge-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.w3c.dom.Document;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.util.XmlConfigUtil;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.XmlConfigUtilTest.XmlConfigUtilTestModule;
import com.koch.ambeth.util.xml.IXmlConfigUtil;
import com.koch.ambeth.util.xml.IXmlValidator;

@TestModule(XmlConfigUtilTestModule.class)
public class XmlConfigUtilTest extends AbstractIocTest {
	private static final String AMBETH_FOLDER = "com/koch/ambeth/";

	private static final String AMBETH_SCHEMA_FOLDER = AMBETH_FOLDER + "schema/";

	private static final String XSD_SIMPLE_TYPES_2_0 =
			AMBETH_SCHEMA_FOLDER + "ambeth_simple_types_2_0.xsd";

	private static final String XSD_ORM_2_0 = AMBETH_SCHEMA_FOLDER + "ambeth_orm_2_0.xsd";

	private static final String ORM_XML_SIMPLE = AMBETH_FOLDER + "util/orm_simple.xml";

	public static class XmlConfigUtilTestModule implements IInitializingModule {

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean("xmlConfigUtil", XmlConfigUtil.class)
					.autowireable(IXmlConfigUtil.class);
		}

	}

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	@Test
	public void testReadXmlFiles() {
		Document[] docs = xmlConfigUtil.readXmlFiles(ORM_XML_SIMPLE);
		assertNotNull(docs);
		assertEquals(1, docs.length);
	}

	@Test
	public void testCreateValidator() throws Exception {
		IXmlValidator validator1 = xmlConfigUtil.createValidator(XSD_SIMPLE_TYPES_2_0);
		assertNotNull(validator1);

		IXmlValidator validator2 = xmlConfigUtil.createValidator(XSD_SIMPLE_TYPES_2_0, XSD_ORM_2_0);
		assertNotNull(validator2);

		Document doc = xmlConfigUtil.readXmlFiles(ORM_XML_SIMPLE)[0];
		validator2.validate(doc);
	}
}
