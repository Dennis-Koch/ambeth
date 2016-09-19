package de.osthus.ambeth.xml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestModule;

@TestModule({ XmlModule.class })
public class CyclicXmlReaderTest extends AbstractInformationBusTest
{
	public static class MyEntity
	{
		private String myValue;

		private boolean myValueSpecified;

		private String myValue2;

		private boolean myValue2Specified;

		public String getMyValue()
		{
			return myValue;
		}

		public void setMyValue(String myValue)
		{
			this.myValue = myValue;
			setMyValueSpecified(true);
		}

		public String getMyValue2()
		{
			return myValue2;
		}

		public void setMyValue2(String myValue2)
		{
			this.myValue2 = myValue2;
			setMyValue2Specified(true);
		}

		public boolean isMyValue2Specified()
		{
			return myValue2Specified;
		}

		public void setMyValue2Specified(boolean myValue2Specified)
		{
			this.myValue2Specified = myValue2Specified;
		}

		public boolean isMyValueSpecified()
		{
			return myValueSpecified;
		}

		public void setMyValueSpecified(boolean myValueSpecified)
		{
			this.myValueSpecified = myValueSpecified;
		}
	}

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@SecurityContext(SecurityContextType.NOT_REQUIRED)
	public void test()
	{

	}

	@SuppressWarnings("unchecked")
	@Test
	public void cyclicTestReadMetaData()
	{
		String xml = "<root><l i=\"1\" s=\"2\" ti=\"2\" n=\"Object\"><o i=\"3\" ti=\"4\" n=\"EntityMetaDataTransfer\" m=\"AlternateIdMemberIndicesInPrimitives AlternateIdMemberNames CreatedByMemberName CreatedOnMemberName EntityType IdMemberName MergeRelevantNames PrimitiveMemberNames RelationMemberNames TypesRelatingToThis TypesToCascadeDelete UpdatedByMemberName UpdatedOnMemberName VersionMemberName\"><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/></o><r i=\"1\"/></l></root>";
		List<Object> list = (List<Object>) cyclicXmlHandler.read(xml);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void readUnspecified()
	{
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
