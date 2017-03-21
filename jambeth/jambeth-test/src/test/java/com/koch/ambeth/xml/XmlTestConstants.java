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

public final class XmlTestConstants
{
	public static final boolean supportsGenericTypeOnCollections = false;

	public static final boolean supportsEnumsAsObjectRef = true;

	public static final String[] XmlOutput = {
			"<root><o i=\"1\" ti=\"2\" n=\"CUDResult\" m=\"AllChanges\"><l i=\"3\" s=\"0\" ti=\"4\" n=\""
					+ (supportsGenericTypeOnCollections ? "com.koch.ambeth.merge.model.IChangeContainer" : "Object") + "\"></l></o></root>",
			"<root><o i=\"1\" ti=\"2\" n=\"EntityMetaDataTransfer\" m=\"AlternateIdMemberIndicesInPrimitives AlternateIdMemberNames CreatedByMemberName CreatedOnMemberName EntityType IdMemberName MergeRelevantNames PrimitiveMemberNames RelationMemberNames TypesRelatingToThis TypesToCascadeDelete UpdatedByMemberName UpdatedOnMemberName VersionMemberName\"><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/><n/></o></root>",
			"<root><o i=\"1\" ti=\"2\" n=\"CUDResult\" m=\"AllChanges\"><l i=\"3\" s=\"0\" ti=\"4\" n=\""
					+ (supportsGenericTypeOnCollections ? "com.koch.ambeth.merge.model.IChangeContainer" : "Object") + "\"></l></o></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Object\"><c i=\"3\" n=\"Class\"/><c i=\"4\" n=\"List\"/><c i=\"5\" n=\"com.koch.ambeth.xml.transfer.TestXmlObject\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Object\"><e i=\"3\" ti=\"4\" n=\"com.koch.ambeth.xml.transfer.TestEnum\" v=\"VALUE_1\"/><e i=\"5\" ti=\"4\" v=\"VALUE_2\"/>"
					+ (supportsEnumsAsObjectRef ? "<r i=\"3\"/>" : "<e i=\"6\" ti=\"4\" v=\"VALUE_1\"/>") + "</a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Object\"><e i=\"3\" ti=\"4\" n=\"com.koch.ambeth.xml.transfer.TestEnum\" v=\"VALUE_1\"/><e i=\"5\" ti=\"4\" v=\"VALUE_2\"/><r i=\"3\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" "
					+ (supportsGenericTypeOnCollections ? "n=\"ListG\" gti=\"3\" gn=\"Object\"><l i=\"4\" s=\"0\" ti=\"3\"></l>"
							: "n=\"List\"><l i=\"3\" s=\"0\" ti=\"4\" n=\"Object\"></l>") + "<l i=\"5\" s=\"0\" ti=\"4\"></l><r i=\"5\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Int32\"><values v=\"2147483647;-2147483648;3\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Int64\"><values v=\"9223372036854775807;-9223372036854775808;3\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Float64\"><values v=\"1.7976931348623157E308;4.9E-324;3.0\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Float32\"><values v=\"3.4028235E38;1.4E-45;3.0\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Int16\"><values v=\"32767;-32768;3\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Byte\"><values v=\"f4AD\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Char\"><values v=\"77+/AAM=\"/></a></root>",
			"<root><a i=\"1\" s=\"3\" ti=\"2\" n=\"Bool\"><values v=\"101\"/></a></root>",
			"<root><or i=\"1\"><c i=\"2\" n=\"com.koch.ambeth.xml.Entity\"/><o i=\"3\" ti=\"4\" n=\"Int32N\" v=\"2\"/><o i=\"5\" ti=\"6\" n=\"Int16N\" v=\"4\"/></or></root>",
			"<root><or i=\"1\" ix=\"0\"><c i=\"2\" n=\"com.koch.ambeth.xml.Entity\"/><s i=\"3\"><![CDATA[zwei]]></s><o i=\"4\" ti=\"5\" n=\"Int16N\" v=\"4\"/></or></root>",
			"<root><or i=\"1\" ix=\"1\"><c i=\"2\" n=\"com.koch.ambeth.xml.Entity\"/><s i=\"3\"><![CDATA[zwei]]></s><o i=\"4\" ti=\"5\" n=\"Int16N\" v=\"4\"/></or></root>" };

	private XmlTestConstants()
	{
		// Intended blank
	}
}
