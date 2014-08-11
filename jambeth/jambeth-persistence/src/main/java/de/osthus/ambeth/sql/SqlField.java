package de.osthus.ambeth.sql;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.Field;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.persistence.IllegalResultException;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class SqlField extends Field
{
	protected ISqlConnection connection;

	protected ISqlBuilder sqlBuilder;

	protected IConversionHelper conversionHelper;

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(connection, "connection");
		ParamChecker.assertNotNull(sqlBuilder, "sqlBuilder");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	public void setConnection(ISqlConnection connection)
	{
		this.connection = connection;
	}

	public void setSqlBuilder(ISqlBuilder sqlBuilder)
	{
		this.sqlBuilder = sqlBuilder;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public IVersionCursor findAll(Object value)
	{
		ITable table = getTable();
		String idFieldName = table.getIdField().getName();
		IField versionField = table.getVersionField();
		String versionFieldName = versionField != null ? versionField.getName() : null;

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		StringBuilder whereSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sqlBuilder.appendName(idFieldName, selectSB);
			selectSB.append(',');
			sqlBuilder.appendName(versionFieldName, selectSB);

			value = conversionHelper.convertValueToType(getFieldType(), value);

			sqlBuilder.appendNameValue(getName(), value, whereSB);

			IResultSet resultSet = connection.selectFields(table.getFullqualifiedEscapedName(), selectSB.toString(), whereSB.toString(), null);

			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(resultSet);
			versionCursor.afterPropertiesSet();

			return versionCursor;
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
			tlObjectCollector.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor findMany(List<?> values)
	{
		ITable table = getTable();
		String idFieldName = table.getIdField().getName();
		IField versionField = table.getVersionField();
		String versionFieldName = versionField != null ? versionField.getName() : null;

		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder selectSB = current.create(StringBuilder.class);
		StringBuilder whereSB = current.create(StringBuilder.class);
		ArrayList<Object> converted = new ArrayList<Object>(values.size());

		try
		{
			this.sqlBuilder.appendName(idFieldName, selectSB);
			selectSB.append(',');
			this.sqlBuilder.appendName(versionFieldName, selectSB);

			for (int a = values.size(); a-- > 0;)
			{
				Object value = values.get(a);
				Object conValue = this.conversionHelper.convertValueToType(getFieldType(), value);

				converted.add(conValue);
			}

			this.sqlBuilder.appendNameValues(getName(), converted, whereSB);

			IResultSet resultSet = this.connection.selectFields(table.getFullqualifiedEscapedName(), selectSB.toString(), whereSB.toString(), null);

			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(resultSet);
			versionCursor.afterPropertiesSet();

			return versionCursor;
		}
		finally
		{
			current.dispose(whereSB);
			current.dispose(selectSB);
		}
	}

	@Override
	public IVersionItem findSingle(Object value)
	{
		ITable table = getTable();
		String idFieldName = table.getIdField().getName();
		IField versionField = table.getVersionField();
		String versionFieldName = versionField != null ? versionField.getName() : null;

		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder selectSB = current.create(StringBuilder.class);
		StringBuilder whereSB = current.create(StringBuilder.class);

		try
		{
			sqlBuilder.appendName(idFieldName, selectSB);
			selectSB.append(',');
			sqlBuilder.appendName(versionFieldName, selectSB);

			value = conversionHelper.convertValueToType(getFieldType(), value);

			sqlBuilder.appendNameValue(getName(), value, whereSB);

			IResultSet resultSet = connection.selectFields(table.getFullqualifiedEscapedName(), selectSB.toString(), whereSB.toString(), null);

			if (resultSet == null)
			{
				return null;
			}
			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(resultSet);
			versionCursor.afterPropertiesSet();
			if (!versionCursor.moveNext())
			{
				versionCursor.dispose();
				return null;
			}
			return versionCursor;
		}
		finally
		{
			current.dispose(whereSB);
			current.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor all(Object value)
	{
		return findAll(value);
	}

	@Override
	public IVersionItem single(Object value) throws IllegalResultException
	{
		IVersionCursor all = findAll(value);
		if (!all.moveNext())
		{
			all.dispose();
			throw new RuntimeException("No element found for value '" + value + "'.");
		}
		IVersionItem single = all.getCurrent();
		if (all.moveNext())
		{
			all.dispose();
			throw new RuntimeException("More than one element found for value '" + value + "'.");
		}
		return single;
	}

	@Override
	public IVersionItem first(Object value) throws IllegalResultException
	{
		IVersionItem first = findSingle(value);
		if (first == null)
		{
			throw new RuntimeException("No element found for value '" + value + "'.");
		}
		return first;
	}

	@Override
	public IVersionItem firstOrDefault(Object value)
	{
		return findSingle(value);
	}
}
