package de.osthus.ambeth.xml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;

@TestModule({ BootstrapScannerModule.class, XmlModule.class, XmlTestModule.class })
public class CyclicXmlReaderTest extends AbstractIocTest
{
	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@SuppressWarnings("unchecked")
	@Test
	public void cyclicTestReadMetaData()
	{
		String xml = "<root><l i=\"1\" s=\"2\" ti=\"2\" n=\"Object\"><o i=\"3\" ti=\"4\" n=\"EntityMetaDataTransfer\" m=\"AlternateIdMemberIndicesInPrimitives AlternateIdMemberNames CreatedByMemberName CreatedOnMemberName EntityType IdMemberName MergeRelevantNames PrimitiveMemberNames RelationMemberNames TypesRelatingToThis TypesToCascadeDelete UpdatedByMemberName UpdatedOnMemberName VersionMemberName\"><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/></o><r i=\"1\"/></l></root>";
		List<Object> list = (List<Object>) cyclicXmlHandler.read(xml);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
	}
}
