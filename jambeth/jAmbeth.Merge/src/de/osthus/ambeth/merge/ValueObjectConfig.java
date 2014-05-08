package de.osthus.ambeth.merge;

import java.util.HashSet;
import java.util.Set;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ValueObjectConfig implements IValueObjectConfig
{
	@SuppressWarnings("unused")
	@LogInstance(ValueObjectConfig.class)
	private ILogger log;

	protected Class<?> entityType;

	protected Class<?> valueType;

	protected Set<String> listTypeMembers = new HashSet<String>();

	protected IMap<String, ValueObjectMemberType> memberTypes = new HashMap<String, ValueObjectMemberType>();

	protected IMap<String, Class<?>> collectionMemberTypes = new HashMap<String, Class<?>>();

	protected IMap<String, String> boToVoMemberNameMap = new HashMap<String, String>();

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

	public void putValueObjectMemberName(String businessObjectMemberName, String valueObjectMemberName)
	{
		if (!boToVoMemberNameMap.putIfNotExists(businessObjectMemberName, valueObjectMemberName))
		{
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
