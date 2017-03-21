package com.koch.ambeth.xml;

/*-
 * #%L
 * jambeth-test
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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.xml.ioc.XmlModule;

@TestModule({XmlModule.class})
public class CyclicXmlReaderTest extends AbstractInformationBusTest {
	public static class MyEntity {
		private String myValue;

		private boolean myValueSpecified;

		private String myValue2;

		private boolean myValue2Specified;

		public String getMyValue() {
			return myValue;
		}

		public void setMyValue(String myValue) {
			this.myValue = myValue;
			setMyValueSpecified(true);
		}

		public String getMyValue2() {
			return myValue2;
		}

		public void setMyValue2(String myValue2) {
			this.myValue2 = myValue2;
			setMyValue2Specified(true);
		}

		public boolean isMyValue2Specified() {
			return myValue2Specified;
		}

		public void setMyValue2Specified(boolean myValue2Specified) {
			this.myValue2Specified = myValue2Specified;
		}

		public boolean isMyValueSpecified() {
			return myValueSpecified;
		}

		public void setMyValueSpecified(boolean myValueSpecified) {
			this.myValueSpecified = myValueSpecified;
		}
	}

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@SecurityContext(SecurityContextType.NOT_REQUIRED)
	public void test() {

	}

	@SuppressWarnings("unchecked")
	@Test
	public void cyclicTestReadMetaData() {
		String xml =
				"<root><l i=\"1\" s=\"2\" ti=\"2\" n=\"Object\"><o i=\"3\" ti=\"4\" n=\"com.koch.ambeth.merge.transfer.EntityMetaDataTransfer\" m=\"AlternateIdMemberIndicesInPrimitives AlternateIdMemberNames CreatedByMemberName CreatedOnMemberName EntityType IdMemberName MergeRelevantNames PrimitiveMemberNames RelationMemberNames TypesRelatingToThis TypesToCascadeDelete UpdatedByMemberName UpdatedOnMemberName VersionMemberName\"><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/></o><r i=\"1\"/></l></root>";
		List<Object> list = (List<Object>) cyclicXmlHandler.read(xml);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void readUnspecified() {
		MyEntity obj1 = new MyEntity();
		obj1.setMyValue("hello");

		String xml = cyclicXmlHandler.write(obj1);

		MyEntity obj2 = (MyEntity) cyclicXmlHandler.read(xml);

		Assert.assertEquals(obj1.getMyValue(), obj2.getMyValue());
		Assert.assertEquals(obj1.getMyValue2(), obj2.getMyValue2());
		Assert.assertEquals(obj1.isMyValueSpecified(), obj2.isMyValueSpecified());
		Assert.assertEquals(obj1.isMyValue2Specified(), obj2.isMyValue2Specified());
	}
}
