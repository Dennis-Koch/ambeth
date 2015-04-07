package de.osthus.ambeth.persistence;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.util.ParamChecker;

public class FieldMetaData implements IFieldMetaData, IInitializingBean
{
	protected IProperties properties;

	protected ITableMetaData table;

	protected String name;

	protected Member member;

	protected Class<?> fieldType;

	protected Class<?> fieldSubType;

	protected boolean isAlternateId;

	protected boolean expectsMapping = true;

	protected byte idIndex = ObjRef.UNDEFINED_KEY_INDEX;

	protected int indexOnTable = -1;

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
	public ITableMetaData getTable()
	{
		return table;
	}

	public void setTable(ITableMetaData table)
	{
		this.table = table;
	}

	@Override
	public boolean expectsMapping()
	{
		return expectsMapping;
	}

	public void setExpectsMapping(boolean expectsMapping)
	{
		this.expectsMapping = expectsMapping;
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
	public String toString()
	{
		return "Field: " + getName();
	}

	@Override
	public int getIndexOnTable()
	{
		return indexOnTable;
	}

	public void setIndexOnTable(int indexOnTable)
	{
		this.indexOnTable = indexOnTable;
	}
}
