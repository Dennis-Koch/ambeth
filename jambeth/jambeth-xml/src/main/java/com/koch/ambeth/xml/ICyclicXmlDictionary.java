package com.koch.ambeth.xml;

/*-
 * #%L
 * jambeth-xml
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
