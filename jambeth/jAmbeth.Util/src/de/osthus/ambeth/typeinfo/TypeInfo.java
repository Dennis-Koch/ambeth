package de.osthus.ambeth.typeinfo;

import java.util.Map;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;

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
		fieldAccess = realType.isInterface() || realType.isPrimitive() ? null : FieldAccess.get(realType);
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

	public boolean doesImplement(Class<?> interfaceArgument)
	{
		if (interfaceArgument.isAssignableFrom(realType))
		{
			return true;
		}
		return false;
	}
}
