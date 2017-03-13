package com.koch.ambeth.xml;

public interface ICyclicXmlDictionary
{
	// Elements
	String getArrayElement();

	String getBlobElement();

	String getClassElement();

	String getEntityRefElement();

	String getListElement();

	String getEnumElement();

	String getExceptionElement();

	String getObjectElement();

	String getRefElement();

	String getRootElement();

	String getSetElement();

	String getNumberElement();

	String getNullElement();

	String getPostProcessElement();

	String getPrimitiveElement();

	String getSimpleObjectElement();

	String getStringElement();

	String getTypeElement();

	String getOriWrapperElement();

	// Attributes
	String getClassIdAttribute();

	String getClassNameAttribute();

	String getClassNamespaceAttribute();

	String getClassMemberAttribute();

	String getSizeAttribute();

	String getArrayDimensionAttribute();

	String getEntityRefKeyAttribute();

	String getEntityRefKeyIndexAttribute();

	String getEntityRefVersionAttribute();

	String getIdAttribute();

	String getValueAttribute();

	String getUnspecifiedElement();
}
