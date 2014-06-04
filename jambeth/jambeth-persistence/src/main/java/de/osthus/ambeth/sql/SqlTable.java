package de.osthus.ambeth.sql;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.ICursor;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.Table;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class SqlTable extends Table
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ISqlConnection sqlConnection;
	protected IPersistenceHelper persistenceHelper;
	protected IContextProvider contextProvider;
	protected ISqlBuilder sqlBuilder;
	protected Object initialVersion;
	protected IConversionHelper conversionHelper;

	protected IConnectionDialect connectionDialect;

	protected IPrimaryKeyProvider primaryKeyProvider;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(sqlConnection, "sqlConnection");
		ParamChecker.assertNotNull(persistenceHelper, "persistenceHelper");
		ParamChecker.assertNotNull(initialVersion, "initialVersion");
		ParamChecker.assertNotNull(contextProvider, "contextProvider");
		ParamChecker.assertNotNull(sqlBuilder, "sqlBuilder");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		ParamChecker.assertNotNull(connectionDialect, "connectionDialect");
		ParamChecker.assertNotNull(primaryKeyProvider, "primaryKeyProvider");
	}

	public void setSqlConnection(ISqlConnection sqlConnection)
	{
		this.sqlConnection = sqlConnection;
	}

	public void setPersistenceHelper(IPersistenceHelper persistenceHelper)
	{
		this.persistenceHelper = persistenceHelper;
	}

	public void setContextProvider(IContextProvider contextProvider)
	{
		this.contextProvider = contextProvider;
	}

	public void setSqlBuilder(ISqlBuilder sqlBuilder)
	{
		this.sqlBuilder = sqlBuilder;
	}

	public void setInitialVersion(Object initialVersion)
	{
		this.initialVersion = initialVersion;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setConnectionDialect(IConnectionDialect connectionDialect)
	{
		this.connectionDialect = connectionDialect;
	}

	public void setPrimaryKeyProvider(IPrimaryKeyProvider primaryKeyProvider)
	{
		this.primaryKeyProvider = primaryKeyProvider;
	}

	@Override
	public IField getFieldByPropertyName(String propertyName)
	{
		IField field = getFieldByMemberName(propertyName);
		if (field == null)
		{
			IDirectedLink link = getLinkByMemberName(propertyName);
			if (link != null)
			{
				field = link.getFromField();
			}
		}
		if (field == null)
		{
			if (getIdField().getMember().getName().equals(propertyName))
			{
				field = getIdField();
			}
			else if (getVersionField().getMember().getName().equals(propertyName))
			{
				field = getVersionField();
			}
		}

		return field;
	}

	@Override
	public void delete(List<IObjRef> oris)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = current.create(StringBuilder.class);
		String[] whereSqls = new String[oris.size()];
		try
		{
			String idFieldName = getIdField().getName();
			IField versionField = getVersionField();
			Class<?> idFieldType = getIdField().getFieldType();
			String versionFieldName = null;
			Class<?> versionFieldType = null;
			if (versionField != null)
			{
				versionFieldName = versionField.getName();
				versionFieldType = versionField.getFieldType();
			}
			for (int i = oris.size(); i-- > 0;)
			{
				IObjRef ori = oris.get(i);
				Object id = conversionHelper.convertValueToType(idFieldType, ori.getId());
				sqlBuilder.appendNameValue(idFieldName, id, sb);
				Object version = ori.getVersion();
				if (version != null && versionField != null)
				{
					version = conversionHelper.convertValueToType(versionFieldType, version);
					sb.append(" AND ");
					sqlBuilder.appendNameValue(versionFieldName, version, sb);
				}
				whereSqls[i] = sb.toString();
				sb.setLength(0);
			}
			sqlConnection.queueDelete(getName(), whereSqls);
		}
		finally
		{
			current.dispose(sb);
		}
	}

	@Override
	public void deleteAll()
	{
		sqlConnection.queueDeleteAll(getName());
	}

	@Override
	public IList<Object> acquireIds(int count)
	{
		return primaryKeyProvider.acquireIds(this, count);
	}

	protected void convertValueToFieldType(Class<?> fieldType, Object value, StringBuilder targetSb)
	{
		try
		{
			if (connectionDialect == null || !connectionDialect.handleField(fieldType, value, targetSb))
			{
				Object convertedValue = conversionHelper.convertValueToType(fieldType, value);
				sqlBuilder.appendValue(convertedValue, targetSb);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public ICursor selectValues(List<?> ids)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			List<IField> fields = getAllFields();

			IField idField = getIdField();
			String idFieldName = idField.getName();

			sqlBuilder.appendName(idFieldName, selectSB);

			IField versionField = getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			ArrayList<IField> cursorFields = new ArrayList<IField>();

			for (int a = fields.size(); a-- > 0;)
			{
				IField field = fields.get(a);
				if (field.getMember() == null)
				{
					// Ignore fields which can not be loaded into entities
					continue;
				}
				cursorFields.add(field);
				selectSB.append(',');
				sqlBuilder.appendName(field.getName(), selectSB);
			}
			ResultSetCursor cursor = new ResultSetCursor();
			cursor.setContainsVersion(versionField != null);
			cursor.setResultSet(sqlConnection.createResultSet(getName(), idFieldName, idField.getFieldType(), selectSB.toString(), null, ids));
			cursor.setFields(cursorFields.toArray(IField.class));
			cursor.afterPropertiesSet();
			return cursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
		}
	}

	@Override
	public ICursor selectValues(String alternateIdMemberName, List<?> alternateIds)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			IField idField = getIdField();
			String primaryIdFieldName = idField.getName();
			String idFieldName = null;
			Class<?> idFieldType = null;

			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IField versionField = getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}
			ArrayList<IField> cursorFields = new ArrayList<IField>();

			List<IField> fields = getAllFields();
			for (int a = fields.size(); a-- > 0;)
			{
				IField field = fields.get(a);
				ITypeInfoItem member = field.getMember();
				if (member == null)
				{
					// Ignore fields which can not be loaded into entities
					continue;
				}
				if (member.getName().equals(alternateIdMemberName))
				{
					idFieldName = field.getName();
					idFieldType = field.getFieldType();
				}
				cursorFields.add(field);
				selectSB.append(',');
				sqlBuilder.appendName(field.getName(), selectSB);
			}
			if (idFieldName == null && idField.getMember().getName().equals(alternateIdMemberName))
			{
				idFieldName = primaryIdFieldName;
				idFieldType = idField.getFieldType();
			}
			if (idFieldName == null)
			{
				throw new IllegalArgumentException("No field mapped to member " + getEntityType().getName() + "." + alternateIdMemberName);
			}
			IResultSet resultSet = sqlConnection.createResultSet(getName(), idFieldName, idFieldType, selectSB.toString(), null, alternateIds);
			ResultSetCursor cursor = new ResultSetCursor();
			cursor.setContainsVersion(versionField != null);
			cursor.setResultSet(resultSet);
			cursor.setFields(cursorFields.toArray(IField.class));
			cursor.afterPropertiesSet();
			return cursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor selectVersion(List<?> ids)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			IField idField = getIdField();
			String idFieldName = idField.getName();

			sqlBuilder.appendName(idFieldName, selectSB);

			IField versionField = getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.createResultSet(getName(), idFieldName, idField.getFieldType(), selectSB.toString(), null, ids));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor selectVersion(String alternateIdMemberName, List<?> alternateIds)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			IField idField = getIdField();
			String primaryIdFieldName = idField.getName();
			String idFieldName = null;

			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IField versionField = getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			IField[] alternateIdFields = getAlternateIdFields();
			for (int a = alternateIdFields.length; a-- > 0;)
			{
				IField field = alternateIdFields[a];
				ITypeInfoItem member = field.getMember();
				if (member == null)
				{
					// Ignore fields which can not be loaded into entities
					continue;
				}
				if (member.getName().equals(alternateIdMemberName))
				{
					idFieldName = field.getName();
				}
				selectSB.append(',');
				sqlBuilder.appendName(field.getName(), selectSB);
			}
			if (idFieldName == null && idField.getMember().getName().equals(alternateIdMemberName))
			{
				idFieldName = idField.getName();
			}
			if (idFieldName == null)
			{
				throw new IllegalArgumentException("No alternate id field mapped to member " + getEntityType().getName() + "." + alternateIdMemberName);
			}
			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.createResultSet(getName(), idFieldName, idField.getFieldType(), selectSB.toString(), null, alternateIds));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereWithOrderBySql, ILinkedMap<Integer, Object> params)
	{
		return selectVersionJoin(additionalSelectColumnList, null, whereWithOrderBySql, params);
	}

	@Override
	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql,
			ILinkedMap<Integer, Object> params)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectVersionJoin(additionalSelectColumnList, joinSql, whereWithOrderBySql, params, tableAlias);
	}

	@Override
	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql,
			ILinkedMap<Integer, Object> params, String tableAlias)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		StringBuilder fieldPatternSB = tlObjectCollector.create(StringBuilder.class);
		HashSet<String> additionalSelectColumnSet = null;
		try
		{
			String primaryIdFieldName = getIdField().getName();

			if (additionalSelectColumnList != null)
			{
				additionalSelectColumnSet = new HashSet<String>();
				for (int a = additionalSelectColumnList.size(); a-- > 0;)
				{
					String additionalSelectColumn = additionalSelectColumnList.get(a);
					// additional columns are already escaped
					additionalSelectColumn = additionalSelectColumn.replace("\"", "");
					additionalSelectColumnSet.add(additionalSelectColumn);
				}
			}
			if (tableAlias != null)
			{
				selectSB.append(tableAlias).append(".");
			}
			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IField versionField = getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				if (tableAlias != null)
				{
					selectSB.append(tableAlias).append(".");
				}
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			IField[] alternateIdFields = getAlternateIdFields();
			for (int a = 0; a < alternateIdFields.length; a++)
			{
				IField field = alternateIdFields[a];
				ITypeInfoItem member = field.getMember();
				if (member == null)
				{
					// Ignore fields which can not be loaded into entities
					continue;
				}
				String fieldName = field.getName();
				if (additionalSelectColumnSet != null)
				{
					additionalSelectColumnSet.remove(fieldName);
				}
				selectSB.append(',');
				if (tableAlias != null)
				{
					selectSB.append(tableAlias).append(".");
				}
				sqlBuilder.appendName(fieldName, selectSB);
			}
			if (additionalSelectColumnSet != null && additionalSelectColumnSet.size() > 0)
			{
				for (String additionalFieldName : additionalSelectColumnSet)
				{
					selectSB.append(',').append(additionalFieldName);
					// selectSB.append(',').append(sqlBuilder.escapeName(additionalFieldName));
				}
			}
			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.selectFields(getName(), selectSB.toString(), joinSql, whereWithOrderBySql, params, tableAlias));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
			tlObjectCollector.dispose(fieldPatternSB);
		}
	}

	@Override
	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			int offset, int length, ILinkedMap<Integer, Object> params)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, offset, length, params, tableAlias);
	}

	@Override
	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			int offset, int length, ILinkedMap<Integer, Object> params, String tableAlias)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			String primaryIdFieldName = getIdField().getName();

			if (tableAlias != null)
			{
				selectSB.append(tableAlias).append(".");
			}
			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IField versionField = getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				if (tableAlias != null)
				{
					selectSB.append(tableAlias).append(".");
				}
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			IField[] alternateIdFields = getAlternateIdFields();
			for (int a = alternateIdFields.length; a-- > 0;)
			{
				IField field = alternateIdFields[a];
				ITypeInfoItem member = field.getMember();
				if (member == null)
				{
					// Ignore fields which can not be loaded into entities
					continue;
				}
				selectSB.append(',');
				if (tableAlias != null)
				{
					selectSB.append(tableAlias).append(".");
				}
				sqlBuilder.appendName(field.getName(), selectSB);
			}
			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.selectFields(getName(), selectSB, joinSql, whereSql, additionalSelectColumnList, orderBySql, offset,
					length, params, tableAlias));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
		}
	}

	@Override
	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql, ILinkedMap<Integer, Object> params)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectDataJoin(selectColumnList, joinSql, whereWithOrderBySql, params, tableAlias);
	}

	@Override
	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereWithOrderBySql,
			ILinkedMap<Integer, Object> params, String tableAlias)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		HashMap<String, Integer> propertyToColIndexMap = new HashMap<String, Integer>();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			for (int a = 0, size = selectColumnList.size(); a < size; a++)
			{
				String additionalFieldName = selectColumnList.get(a);
				// additionaFieldName is expected to be already escaped at this point. No need to double escape
				if (a > 0)
				{
					selectSB.append(',');
				}
				selectSB.append(additionalFieldName);
				propertyToColIndexMap.put(selectColumnList.get(a), Integer.valueOf(a));
			}
			ResultSetDataCursor dataCursor = new ResultSetDataCursor();
			dataCursor.setPropertyToColIndexMap(propertyToColIndexMap);
			dataCursor.setResultSet(sqlConnection.selectFields(getName(), selectSB.toString(), joinSql, whereWithOrderBySql, params, tableAlias));
			dataCursor.afterPropertiesSet();
			return dataCursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
		}
	}

	@Override
	public IDataCursor selectDataPaging(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, int offset,
			int length, ILinkedMap<Integer, Object> params)
	{
		HashMap<String, Integer> propertyToColIndexMap = new HashMap<String, Integer>();
		for (int a = 0, size = selectColumnList.size(); a < size; a++)
		{
			propertyToColIndexMap.put(selectColumnList.get(a), Integer.valueOf(a));
		}
		ResultSetDataCursor dataCursor = new ResultSetDataCursor();
		dataCursor.setPropertyToColIndexMap(propertyToColIndexMap);
		dataCursor.setResultSet(sqlConnection.selectFields(getName(), "", joinSql, whereSql, selectColumnList, orderBySql, offset, length, params));
		dataCursor.afterPropertiesSet();
		return dataCursor;
	}

	@Override
	public IVersionCursor selectAll()
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder selectSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			sqlBuilder.appendName(getIdField().getName(), selectSB);

			IField versionField = getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.selectFields(getName(), selectSB.toString(), null, null));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			tlObjectCollector.dispose(selectSB);
		}
	}
}