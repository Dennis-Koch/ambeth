package de.osthus.ambeth.sql;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class MaxValuePrimaryKeyProvider implements IInitializingBean, IPrimaryKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance(MaxValuePrimaryKeyProvider.class)
	private ILogger log;

	protected ISqlConnection sqlConnection;

	protected IConversionHelper conversionHelper;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(sqlConnection, "sqlConnection");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
	}

	public void setSqlConnection(ISqlConnection sqlConnection)
	{
		this.sqlConnection = sqlConnection;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	@Override
	public IList<Object> acquireIds(ITable table, int count)
	{
		IField idField = table.getIdField();
		ArrayList<Object> ids = new ArrayList<Object>();
		Class<?> fieldType = idField.getFieldType();
		IResultSet resultSet = null;

		try
		{
			resultSet = sqlConnection.selectFields(table.getFullqualifiedEscapedName(), "MAX(\"" + idField.getName() + "\")", null, null);
			if (!resultSet.moveNext())
			{
				throw new IllegalArgumentException();
			}
			long maxValue = conversionHelper.convertValueToType(Long.class, resultSet.getCurrent()[0]);
			for (int a = count; a-- > 0;)
			{
				ids.add(conversionHelper.convertValueToType(fieldType, ++maxValue));
			}
		}
		finally
		{
			if (resultSet != null)
			{
				resultSet.dispose();
				resultSet = null;
			}
		}

		return ids;
	}
}
