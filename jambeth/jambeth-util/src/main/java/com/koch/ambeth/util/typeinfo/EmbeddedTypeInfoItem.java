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

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class EmbeddedTypeInfoItem implements ITypeInfoItem, IEmbeddedTypeInfoItem
{
	protected ITypeInfoItem childMember;

	protected ITypeInfoItem[] memberPath;

	protected String name;

	public EmbeddedTypeInfoItem(String name, ITypeInfoItem childMember, ITypeInfoItem... memberPath)
	{
		this.name = name;
		this.childMember = childMember;
		this.memberPath = memberPath;
	}

	@Override
	public ITypeInfoItem[] getMemberPath()
	{
		return memberPath;
	}

	@Override
	public String getMemberPathString()
	{
		StringBuilder sb = new StringBuilder();
		for (ITypeInfoItem member : getMemberPath())
		{
			if (sb.length() > 0)
			{
				sb.append('.');
			}
			sb.append(member.getName());
		}
		return sb.toString();
	}

	@Override
	public String[] getMemberPathToken()
	{
		ITypeInfoItem[] memberPath = getMemberPath();
		String[] token = new String[memberPath.length];
		for (int a = memberPath.length; a-- > 0;)
		{
			ITypeInfoItem member = memberPath[a];
			token[a] = member.getName();
		}
		return token;
	}

	@Override
	public ITypeInfoItem getChildMember()
	{
		return childMember;
	}

	@Override
	public Class<?> getDeclaringType()
	{
		return childMember.getDeclaringType();
	}

	@Override
	public Object getDefaultValue()
	{
		return childMember.getDefaultValue();
	}

	@Override
	public Collection<?> createInstanceOfCollection()
	{
		return childMember.createInstanceOfCollection();
	}

	@Override
	public void setDefaultValue(Object defaultValue)
	{
		childMember.setDefaultValue(defaultValue);
	}

	@Override
	public Object getNullEquivalentValue()
	{
		return childMember.getNullEquivalentValue();
	}

	@Override
	public void setNullEquivalentValue(Object nullEquivalentValue)
	{
		childMember.setNullEquivalentValue(nullEquivalentValue);
	}

	@Override
	public Class<?> getRealType()
	{
		return childMember.getRealType();
	}

	@Override
	public Class<?> getElementType()
	{
		return childMember.getElementType();
	}

	@Override
	public Object getValue(Object obj)
	{
		return getValue(obj, false);
	}

	@Override
	public Object getValue(Object obj, boolean allowNullEquivalentValue)
	{
		Object currentObj = obj;
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			ITypeInfoItem memberPathItem = memberPath[a];
			currentObj = memberPathItem.getValue(currentObj, allowNullEquivalentValue);
			if (currentObj == null)
			{
				if (allowNullEquivalentValue)
				{
					return childMember.getNullEquivalentValue();
				}
				return null;
			}
		}
		return childMember.getValue(currentObj, allowNullEquivalentValue);
	}

	@Override
	public void setValue(Object obj, Object value)
	{
		Object currentObj = obj;
		for (int a = 0, size = memberPath.length; a < size; a++)
		{
			ITypeInfoItem memberPathItem = memberPath[a];
			Object childObj = memberPathItem.getValue(currentObj, false);
			if (childObj == null)
			{
				try
				{
					childObj = memberPathItem.getRealType().newInstance();
				}
				catch (InstantiationException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				catch (IllegalAccessException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				memberPathItem.setValue(currentObj, childObj);
			}
			currentObj = childObj;
		}
		childMember.setValue(currentObj, value);
	}

	@Override
	public <V extends Annotation> V getAnnotation(Class<V> annotationType)
	{
		return childMember.getAnnotation(annotationType);
	}

	@Override
	public boolean canRead()
	{
		return childMember.canRead();
	}

	@Override
	public boolean canWrite()
	{
		return childMember.canWrite();
	}

	@Override
	public boolean isTechnicalMember()
	{
		return childMember.isTechnicalMember();
	}

	@Override
	public void setTechnicalMember(boolean technicalMember)
	{
		childMember.setTechnicalMember(technicalMember);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getXMLName()
	{
		return childMember.getXMLName();
	}

	@Override
	public boolean isXMLIgnore()
	{
		return childMember.isXMLIgnore();
	}

	@Override
	public String toString()
	{
		return "Embedded: " + getName() + "/" + getXMLName() + " " + childMember;
	}
}
