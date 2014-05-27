package de.osthus.ambeth.sql;

import java.util.HashMap;
import java.util.Map;

import de.osthus.ambeth.persistence.ICursor;
import de.osthus.ambeth.persistence.ICursorItem;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.util.IDisposable;

public class ResultSetCursor extends ResultSetVersionCursor implements ICursor, ICursorItem, IDisposable
{
	public static final String SENSOR_NAME = "de.osthus.ambeth.sql.ResultSetCursor";

	protected final Map<String, IField> memberNameToFieldDict = new HashMap<String, IField>();
	protected final Map<String, Integer> memberNameToFieldIndexDict = new HashMap<String, Integer>();
	protected final Map<String, Integer> fieldNameToFieldIndexDict = new HashMap<String, Integer>();

	protected Object[] values;
	protected IField[] fields;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();

		IField[] fields = this.fields;
		this.values = new Object[fields.length];
		for (int a = fields.length; a-- > 0;)
		{
			IField field = fields[a];
			if (field.getMember() != null)
			{
				String memberName = field.getMember().getName();
				this.memberNameToFieldDict.put(memberName, field);
				this.memberNameToFieldIndexDict.put(memberName, a + systemColumnCount);
			}
			this.fieldNameToFieldIndexDict.put(field.getName(), a + systemColumnCount);
		}
	}

	@Override
	public IField[] getFields()
	{
		return this.fields;
	}

	public void setFields(IField[] fields)
	{
		this.fields = fields;
	}

	@Override
	public Object[] getValues()
	{
		return this.values;
	}

	@Override
	public boolean moveNext()
	{
		if (super.moveNext())
		{
			Object[] current = getResultSet().getCurrent();
			IField[] fields = this.fields;
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
	public IField getFieldByMemberName(String memberName)
	{
		return this.memberNameToFieldDict.get(memberName);
	}

	@Override
	public int getFieldIndexByMemberName(String memberName)
	{
		return this.memberNameToFieldIndexDict.get(memberName);
	}

	@Override
	public int getFieldIndexByName(String fieldName)
	{
		return this.fieldNameToFieldIndexDict.get(fieldName);
	}

}
