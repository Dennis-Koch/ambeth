package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
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

import java.util.HashSet;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.ValueObjectMemberType;
import com.koch.ambeth.util.collections.HashMap;

public class ValueObjectConfig implements IValueObjectConfig
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected Class<?> entityType;

	protected Class<?> valueType;

	protected final HashSet<String> listTypeMembers = new HashSet<String>();

	protected final HashMap<String, ValueObjectMemberType> memberTypes = new HashMap<String, ValueObjectMemberType>();

	protected final HashMap<String, Class<?>> collectionMemberTypes = new HashMap<String, Class<?>>();

	protected final HashMap<String, String> boToVoMemberNameMap = new HashMap<String, String>();

	protected final HashMap<String, String> voToBoMemberNameMap = new HashMap<String, String>();

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	@Override
	public Class<?> getValueType()
	{
		return valueType;
	}

	public void setValueType(Class<?> valueType)
	{
		this.valueType = valueType;
	}

	@Override
	public String getValueObjectMemberName(String businessObjectMemberName)
	{
		String voMemberName = boToVoMemberNameMap.get(businessObjectMemberName);
		if (voMemberName == null)
		{
			return businessObjectMemberName;
		}
		else
		{
			return voMemberName;
		}
	}

	public String getBusinessObjectMemberName(String valueObjectMemberName)
	{
		String boMemberName = voToBoMemberNameMap.get(valueObjectMemberName);
		if (boMemberName == null)
		{
			return valueObjectMemberName;
		}
		else
		{
			return boMemberName;
		}
	}

	public void putValueObjectMemberName(String businessObjectMemberName, String valueObjectMemberName)
	{
		if (!boToVoMemberNameMap.putIfNotExists(businessObjectMemberName, valueObjectMemberName))
		{
			throw new IllegalStateException("Mapping for member '" + businessObjectMemberName + "' already defined");
		}
		if (!voToBoMemberNameMap.putIfNotExists(businessObjectMemberName, valueObjectMemberName))
		{
			boToVoMemberNameMap.remove(businessObjectMemberName);
			throw new IllegalStateException("Mapping for member '" + businessObjectMemberName + "' already defined");
		}
	}

	@Override
	public boolean holdsListType(String memberName)
	{
		return listTypeMembers.contains(memberName);
	}

	public void addListTypeMember(String memberName)
	{
		listTypeMembers.add(memberName);
	}

	@Override
	public ValueObjectMemberType getValueObjectMemberType(String valueObjectMemberName)
	{
		ValueObjectMemberType memberType = memberTypes.get(valueObjectMemberName);
		if (memberType == null)
		{
			memberType = ValueObjectMemberType.UNDEFINED;
		}
		return memberType;
	}

	public void setValueObjectMemberType(String valueObjectMemberName, ValueObjectMemberType memberType)
	{
		if (!memberTypes.putIfNotExists(valueObjectMemberName, memberType))
		{
			throw new IllegalStateException("Type entry for member '" + valueObjectMemberName + "' already exists");
		}
	}

	@Override
	public boolean isIgnoredMember(String valueObjectMemberName)
	{
		ValueObjectMemberType memberType = getValueObjectMemberType(valueObjectMemberName);
		return memberType == ValueObjectMemberType.IGNORE;
	}

	@Override
	public Class<?> getMemberType(String memberName)
	{
		return collectionMemberTypes.get(memberName);
	}

	public void putMemberType(String memberName, Class<?> elementType)
	{
		if (!collectionMemberTypes.putIfNotExists(memberName, elementType))
		{
			throw new IllegalStateException("Type for member '" + memberName + "' already defined");
		}
	}
}
