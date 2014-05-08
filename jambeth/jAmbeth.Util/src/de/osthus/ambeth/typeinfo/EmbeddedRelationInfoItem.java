package de.osthus.ambeth.typeinfo;

import java.lang.annotation.Annotation;
import java.util.Collection;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class EmbeddedRelationInfoItem implements IEmbeddedTypeInfoItem, IRelationInfoItem
{
	protected IRelationInfoItem childMember;

	protected ITypeInfoItem[] memberPath;

	protected String name;

	public EmbeddedRelationInfoItem(String name, IRelationInfoItem childMember, ITypeInfoItem... memberPath)
	{
		this.name = name;
		this.childMember = childMember;
		this.memberPath = memberPath;
	}

	@Override
	public IRelationInfoItem getChildMember()
	{
		return childMember;
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
	public void setDefaultValue(Object defaultValue)
	{
		childMember.setDefaultValue(defaultValue);
	}

	@Override
	public Collection<?> createInstanceOfCollection()
	{
		return childMember.createInstanceOfCollection();
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
		return false;
	}

	@Override
	public void setTechnicalMember(boolean technicalMember)
	{
		throw new UnsupportedOperationException("A relation can never be a technical member");
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

	@Override
	public CascadeLoadMode getCascadeLoadMode()
	{
		return childMember.getCascadeLoadMode();
	}

	@Override
	public boolean isManyTo()
	{
		return childMember.isManyTo();
	}

	@Override
	public boolean isToMany()
	{
		return childMember.isToMany();
	}
}
