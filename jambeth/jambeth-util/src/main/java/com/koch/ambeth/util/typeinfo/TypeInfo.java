package com.koch.ambeth.util.typeinfo;

/*-
 * #%L
 * jambeth-util
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

import java.util.Map;

import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.util.WrapperTypeSet;
import com.koch.ambeth.util.collections.HashMap;

public class TypeInfo implements ITypeInfo
{
	protected final String simpleName;

	protected final String toStringValue;

	protected ITypeInfoItem[] members;

	protected final Class<?> realType;

	protected final Map<String, ITypeInfoItem> nameToMemberDict = new HashMap<String, ITypeInfoItem>(0.5f);

	protected final Map<String, ITypeInfoItem> nameToXmlMemberDict = new HashMap<String, ITypeInfoItem>(0.5f);

	protected final FieldAccess fieldAccess;

	public TypeInfo(Class<?> realType)
	{
		this.realType = realType;

		fieldAccess = realType.isInterface() || realType.isPrimitive() || WrapperTypeSet.getUnwrappedType(realType) != null ? null : FieldAccess.get(realType);
		simpleName = realType.getSimpleName().intern();
		toStringValue = realType.toString().intern();
	}

	@Override
	public String getSimpleName()
	{
		return simpleName;
	}

	@Override
	public ITypeInfoItem[] getMembers()
	{
		return members;
	}

	@Override
	public Class<?> getRealType()
	{
		return realType;
	}

	public FieldAccess getFieldAccess()
	{
		return fieldAccess;
	}

	@Override
	public String toString()
	{
		return toStringValue;
	}

	public void postInit(ITypeInfoItem[] members)
	{
		this.members = members;

		for (ITypeInfoItem member : this.members)
		{
			nameToMemberDict.put(member.getName(), member);
			nameToXmlMemberDict.put(member.getXMLName(), member);
		}
	}

	@Override
	public ITypeInfoItem getMemberByName(String memberName)
	{
		return nameToMemberDict.get(memberName);
	}

	@Override
	public ITypeInfoItem getMemberByXmlName(String xmlMemberName)
	{
		return nameToXmlMemberDict.get(xmlMemberName);
	}

	@Override
	public boolean doesImplement(Class<?> interfaceArgument)
	{
		if (interfaceArgument.isAssignableFrom(realType))
		{
			return true;
		}
		return false;
	}
}
