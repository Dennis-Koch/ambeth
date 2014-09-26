package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.ParamChecker;

public class Field implements IField, IInitializingBean
{
	protected IProperties properties;

	protected ITable table;

	protected String name;

	protected Member member;

	protected Class<?> fieldType;

	protected Class<?> fieldSubType;

	protected boolean isAlternateId;

	protected byte idIndex = ObjRef.UNDEFINED_KEY_INDEX;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(properties, "properties");
		ParamChecker.assertNotNull(table, "table");
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(fieldType, "fieldType");
	}

	public void setProperties(IProperties properties)
	{
		this.properties = properties;
	}

	@Override
	public ITable getTable()
	{
		return table;
	}

	public void setTable(ITable table)
	{
		this.table = table;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean isAlternateId()
	{
		return isAlternateId;
	}

	public void setAlternateId()
	{
		isAlternateId = true;
	}

	@Override
	public byte getIdIndex()
	{
		return idIndex;
	}

	public void setIdIndex(byte idIndex)
	{
		this.idIndex = idIndex;
	}

	@Override
	public Member getMember()
	{
		return member;
	}

	public void setMember(Member member)
	{
		if (this.member == member)
		{
			return;
		}
		if (this.member != null && !this.member.getName().equals(member.getName()))
		{
			throw new IllegalStateException("Member already configured and can not be changed later. A call to this method here is a bug");
		}
		this.member = member;
	}

	@Override
	public Class<?> getFieldType()
	{
		return fieldType;
	}

	public void setFieldType(Class<?> fieldType)
	{
		this.fieldType = fieldType;
	}

	public void setFieldSubType(Class<?> fieldSubType)
	{
		this.fieldSubType = fieldSubType;
	}

	@Override
	public Class<?> getFieldSubType()
	{
		return fieldSubType;
	}

	@Override
	public Class<?> getEntityType()
	{
		if (table == null)
		{
			return null;
		}
		return table.getEntityType();
	}

	@Override
	public IVersionCursor findAll(Object value)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor findMany(List<?> values)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionItem findSingle(Object value)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionCursor all(Object value)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionItem single(Object value) throws IllegalResultException
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionItem first(Object value) throws IllegalResultException
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IVersionItem firstOrDefault(Object value)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public String toString()
	{
		return "Field: " + getName();
	}
}
