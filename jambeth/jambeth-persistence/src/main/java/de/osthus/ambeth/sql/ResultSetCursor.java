package de.osthus.ambeth.sql;

import java.util.HashMap;
import java.util.Map;

import de.osthus.ambeth.persistence.ICursor;
import de.osthus.ambeth.persistence.ICursorItem;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.util.IDisposable;

public class ResultSetCursor extends ResultSetVersionCursor implements ICursor, ICursorItem, IDisposable
{
	public static final String SENSOR_NAME = "de.osthus.ambeth.sql.ResultSetCursor";

	protected final Map<String, IFieldMetaData> memberNameToFieldDict = new HashMap<String, IFieldMetaData>();
	protected final Map<String, Integer> memberNameToFieldIndexDict = new HashMap<String, Integer>();
	protected final Map<String, Integer> fieldNameToFieldIndexDict = new HashMap<String, Integer>();

	protected Object[] values;
	protected IFieldMetaData[] fields;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();

		IFieldMetaData[] fields = this.fields;
		values = new Object[fields.length];
		for (int a = fields.length; a-- > 0;)
		{
			IFieldMetaData field = fields[a];
			Integer index = Integer.valueOf(a + systemColumnCount);
			if (field.getMember() != null)
			{
				String memberName = field.getMember().getName();
				memberNameToFieldDict.put(memberName, field);
				memberNameToFieldIndexDict.put(memberName, index);
			}
			fieldNameToFieldIndexDict.put(field.getName(), index);
		}
	}

	@Override
	public IFieldMetaData[] getFields()
	{
		return fields;
	}

	public void setFields(IFieldMetaData[] fields)
	{
		this.fields = fields;
	}

	@Override
	public Object[] getValues()
	{
		return values;
	}

	@Override
	public boolean moveNext()
	{
		if (super.moveNext())
		{
			Object[] current = getResultSet().getCurrent();
			IFieldMetaData[] fields = this.fields;
			int systemColumnCount = this.systemColumnCount;
			Object[] values = this.values;
			for (int a = fields.length; a-- > 0;)
			{
				values[a] = current[a + systemColumnCount];
			}
			return true;
		}
		return false;
	}

	@Override
	public ICursorItem getCurrent()
	{
		return this;
	}

	@Override
	public IFieldMetaData getFieldByMemberName(String memberName)
	{
		return memberNameToFieldDict.get(memberName);
	}

	@Override
	public int getFieldIndexByMemberName(String memberName)
	{
		return memberNameToFieldIndexDict.get(memberName);
	}

	@Override
	public int getFieldIndexByName(String fieldName)
	{
		return fieldNameToFieldIndexDict.get(fieldName);
	}

}
