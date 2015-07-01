package de.osthus.ambeth.sql;

import java.util.List;
import java.util.regex.Pattern;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.ICursor;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.Table;
import de.osthus.ambeth.util.IConversionHelper;

public class SqlTable extends Table
{
	public static final Pattern quotesPattern = Pattern.compile("\"", Pattern.LITERAL);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IContextProvider contextProvider;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Autowired
	protected ISqlConnection sqlConnection;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Override
	public void delete(List<IObjRef> oris)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		ISqlBuilder sqlBuilder = this.sqlBuilder;
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder sb = objectCollector.create(AppendableStringBuilder.class);
		CharSequence[] whereSqls = new CharSequence[oris.size()];
		try
		{
			ITableMetaData metaData = getMetaData();
			IFieldMetaData idField = metaData.getIdField();
			String idFieldName = idField.getName();
			IFieldMetaData versionField = metaData.getVersionField();
			Class<?> idFieldType = idField.getFieldType();
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
				sb.reset();
			}
			sqlConnection.queueDelete(getMetaData().getFullqualifiedEscapedName(), whereSqls);
		}
		finally
		{
			objectCollector.dispose(sb);
		}
	}

	@Override
	public void deleteAll()
	{
		sqlConnection.queueDeleteAll(getMetaData().getFullqualifiedEscapedName());
	}

	@Override
	public ICursor selectValues(List<?> ids)
	{
		ITableMetaData metaData = getMetaData();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder selectSB = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			List<IFieldMetaData> fields = metaData.getAllFields();

			IFieldMetaData idField = metaData.getIdField();
			String idFieldName = idField.getName();

			sqlBuilder.appendName(idFieldName, selectSB);

			IFieldMetaData versionField = metaData.getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			ArrayList<IFieldMetaData> cursorFields = new ArrayList<IFieldMetaData>();

			for (int a = fields.size(); a-- > 0;)
			{
				IFieldMetaData field = fields.get(a);
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
			cursor.setResultSet(sqlConnection.createResultSet(getMetaData().getFullqualifiedEscapedName(), idFieldName, idField.getFieldType(),
					selectSB.toString(), null, ids));
			cursor.setFields(cursorFields.toArray(IFieldMetaData.class));
			cursor.afterPropertiesSet();
			return cursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}

	@Override
	public ICursor selectValues(String alternateIdMemberName, List<?> alternateIds)
	{
		ITableMetaData metaData = getMetaData();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder selectSB = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			IFieldMetaData idField = metaData.getIdField();
			String primaryIdFieldName = idField.getName();
			String idFieldName = null;
			Class<?> idFieldType = null;

			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IFieldMetaData versionField = metaData.getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}
			ArrayList<IFieldMetaData> cursorFields = new ArrayList<IFieldMetaData>();

			List<IFieldMetaData> fields = metaData.getAllFields();
			for (int a = fields.size(); a-- > 0;)
			{
				IFieldMetaData field = fields.get(a);
				Member member = field.getMember();
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
				throw new IllegalArgumentException("No field mapped to member " + getMetaData().getEntityType().getName() + "." + alternateIdMemberName);
			}
			IResultSet resultSet = sqlConnection.createResultSet(getMetaData().getFullqualifiedEscapedName(), idFieldName, idFieldType, selectSB.toString(),
					null, alternateIds);
			ResultSetCursor cursor = new ResultSetCursor();
			cursor.setContainsVersion(versionField != null);
			cursor.setResultSet(resultSet);
			cursor.setFields(cursorFields.toArray(IFieldMetaData.class));
			cursor.afterPropertiesSet();
			return cursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor selectVersion(List<?> ids)
	{
		ITableMetaData metaData = getMetaData();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder selectSB = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			IFieldMetaData idField = metaData.getIdField();
			String idFieldName = idField.getName();

			sqlBuilder.appendName(idFieldName, selectSB);

			IFieldMetaData versionField = metaData.getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.createResultSet(getMetaData().getFullqualifiedEscapedName(), idFieldName, idField.getFieldType(),
					selectSB.toString(), null, ids));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor selectVersion(String alternateIdMemberName, List<?> alternateIds)
	{
		ITableMetaData metaData = getMetaData();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder selectSB = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			IFieldMetaData idField = metaData.getIdField();
			String primaryIdFieldName = idField.getName();
			String idFieldName = null;

			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IFieldMetaData versionField = metaData.getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			IFieldMetaData[] alternateIdFields = metaData.getAlternateIdFields();
			for (int a = alternateIdFields.length; a-- > 0;)
			{
				IFieldMetaData field = alternateIdFields[a];
				Member member = field.getMember();
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
				throw new IllegalArgumentException("No alternate id field mapped to member " + getMetaData().getEntityType().getName() + "."
						+ alternateIdMemberName);
			}
			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.createResultSet(getMetaData().getFullqualifiedEscapedName(), idFieldName, idField.getFieldType(),
					selectSB.toString(), null, alternateIds));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}

	@Override
	public IVersionCursor selectVersionWhere(List<String> additionalSelectColumnList, CharSequence whereSql, CharSequence orderBySql, CharSequence limitSql,
			List<Object> parameters)
	{
		return selectVersionJoin(additionalSelectColumnList, null, whereSql, orderBySql, limitSql, parameters);
	}

	@Override
	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectVersionJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias, true);
	}

	@Override
	public IVersionCursor selectVersionJoin(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, List<Object> parameters, String tableAlias, boolean retrieveAlternateIds)
	{
		ITableMetaData metaData = getMetaData();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder selectSB = objectCollector.create(AppendableStringBuilder.class);
		HashSet<String> additionalSelectColumnSet = null;
		try
		{
			String primaryIdFieldName = metaData.getIdField().getName();

			if (additionalSelectColumnList != null)
			{
				additionalSelectColumnSet = new HashSet<String>();
				for (int a = additionalSelectColumnList.size(); a-- > 0;)
				{
					String additionalSelectColumn = additionalSelectColumnList.get(a);
					// additional columns are already escaped
					additionalSelectColumn = quotesPattern.matcher(additionalSelectColumn).replaceAll("");
					additionalSelectColumnSet.add(additionalSelectColumn);
				}
			}
			if (tableAlias != null)
			{
				selectSB.append(tableAlias).append(".");
			}
			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IFieldMetaData versionField = metaData.getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				if (tableAlias != null)
				{
					selectSB.append(tableAlias).append(".");
				}
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			if (retrieveAlternateIds)
			{
				int akCount = 0;
				IFieldMetaData[] alternateIdFields = metaData.getAlternateIdFields();
				for (int a = 0; a < alternateIdFields.length; a++)
				{
					IFieldMetaData field = alternateIdFields[a];
					Member member = field.getMember();
					if (member == null)
					{
						// Ignore fields which can not be loaded into entities
						continue;
					}
					String fieldName = field.getName();
					if (additionalSelectColumnSet != null)
					{
						additionalSelectColumnSet.remove(fieldName);
						if (tableAlias != null)
						{
							additionalSelectColumnSet.remove(tableAlias + "." + fieldName);
						}
					}
					selectSB.append(',');
					if (tableAlias != null)
					{
						selectSB.append(tableAlias).append(".");
					}
					sqlBuilder.appendName(fieldName, selectSB);

					// JH To fix ticket https://jira.osthus.de/browse/AMBETH-498
					// When ordering by an AK it is selected twice. So one needs an alias.
					if (additionalSelectColumnList.contains("\"" + fieldName + "\""))
					{
						selectSB.append(" AS AK").append(Integer.toString(akCount++));
					}
				}
			}
			if (additionalSelectColumnSet != null && additionalSelectColumnSet.size() > 0)
			{
				for (String additionalFieldName : additionalSelectColumnSet)
				{
					selectSB.append(',');
					String[] schemaAndTableName = sqlBuilder.getSchemaAndTableName(additionalFieldName);
					if (schemaAndTableName[0] != null)
					{
						selectSB.append(schemaAndTableName[0]).append('.');
					}
					// JH 2015-04-28: Field names have to be escaped. Fields from orderBy are processed here.
					sqlBuilder.appendName(schemaAndTableName[1], selectSB);
				}
			}

			String fqTableName = getMetaData().getFullqualifiedEscapedName();
			IResultSet selectResult = sqlConnection.selectFields(fqTableName, selectSB, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias);

			ResultSetPkVersionCursor versionCursor = retrieveAlternateIds ? new ResultSetVersionCursor() : new ResultSetPkVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(selectResult);
			versionCursor.afterPropertiesSet();

			return versionCursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}

	@Override
	public long selectCountJoin(CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql, List<Object> parameters, String tableAlias)
	{
		IResultSet resultSet = sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), "COUNT(*)", joinSql, whereSql, orderBySql, null,
				parameters, tableAlias);
		try
		{
			if (!resultSet.moveNext())
			{
				return 0;
			}
			Object[] countValues = resultSet.getCurrent();
			return ((Number) countValues[0]).longValue();
		}
		finally
		{
			resultSet.dispose();
		}
	}

	@Override
	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, offset, length, parameters, tableAlias, true);
	}

	@Override
	public IVersionCursor selectVersionPaging(List<String> additionalSelectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters, String tableAlias, boolean retrieveAlternateIds)
	{
		ITableMetaData metaData = getMetaData();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder selectSB = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			String primaryIdFieldName = metaData.getIdField().getName();

			if (tableAlias != null)
			{
				selectSB.append(tableAlias).append(".");
			}
			sqlBuilder.appendName(primaryIdFieldName, selectSB);

			IFieldMetaData versionField = metaData.getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				if (tableAlias != null)
				{
					selectSB.append(tableAlias).append(".");
				}
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			if (retrieveAlternateIds)
			{
				IFieldMetaData[] alternateIdFields = metaData.getAlternateIdFields();
				int akCount = 0;
				for (int a = alternateIdFields.length; a-- > 0;)
				{
					IFieldMetaData field = alternateIdFields[a];
					Member member = field.getMember();
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
					String fieldName = field.getName();
					sqlBuilder.appendName(fieldName, selectSB);

					// To fix ticket https://jira.osthus.de/browse/AMBETH-498
					// When ordering by an AK it is selected twice. So one needs an alias.
					if (additionalSelectColumnList.contains("\"" + fieldName + "\""))
					{
						selectSB.append(" AS AK").append(Integer.toString(akCount++));
					}
				}
			}

			String fqTableName = getMetaData().getFullqualifiedEscapedName();
			IResultSet selectResult = sqlConnection.selectFields(fqTableName, selectSB, joinSql, whereSql, additionalSelectColumnList, orderBySql, limitSql,
					offset, length, parameters, tableAlias);

			ResultSetPkVersionCursor versionCursor = retrieveAlternateIds ? new ResultSetVersionCursor() : new ResultSetPkVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(selectResult);
			versionCursor.afterPropertiesSet();

			return versionCursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}

	@Override
	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitBySql, List<Object> parameters)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectDataJoin(selectColumnList, joinSql, whereSql, orderBySql, limitBySql, parameters, tableAlias);
	}

	@Override
	public IDataCursor selectDataJoin(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitBySql, List<Object> parameters, String tableAlias)
	{
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		HashMap<String, Integer> propertyToColIndexMap = new HashMap<String, Integer>();
		StringBuilder selectSB = objectCollector.create(StringBuilder.class);
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
			dataCursor.setResultSet(sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), selectSB, joinSql, whereSql, orderBySql,
					limitBySql, parameters, tableAlias));
			dataCursor.afterPropertiesSet();
			return dataCursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}

	@Override
	public IDataCursor selectDataPaging(List<String> selectColumnList, CharSequence joinSql, CharSequence whereSql, CharSequence orderBySql,
			CharSequence limitSql, int offset, int length, List<Object> parameters)
	{
		int size = selectColumnList.size();
		IMap<String, Integer> propertyToColIndexMap = size > 0 ? HashMap.<String, Integer> create(size) : EmptyMap.<String, Integer> emptyMap();
		for (int a = 0; a < size; a++)
		{
			propertyToColIndexMap.put(selectColumnList.get(a), Integer.valueOf(a));
		}
		ResultSetDataCursor dataCursor = new ResultSetDataCursor();
		dataCursor.setPropertyToColIndexMap(propertyToColIndexMap);
		dataCursor.setResultSet(sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), "", joinSql, whereSql, selectColumnList, orderBySql,
				limitSql, offset, length, parameters));
		dataCursor.afterPropertiesSet();
		return dataCursor;
	}

	@Override
	public IVersionCursor selectAll()
	{
		ITableMetaData metaData = getMetaData();
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		AppendableStringBuilder selectSB = objectCollector.create(AppendableStringBuilder.class);
		try
		{
			sqlBuilder.appendName(metaData.getIdField().getName(), selectSB);

			IFieldMetaData versionField = metaData.getVersionField();
			if (versionField != null)
			{
				selectSB.append(',');
				sqlBuilder.appendName(versionField.getName(), selectSB);
			}

			ResultSetVersionCursor versionCursor = new ResultSetVersionCursor();
			versionCursor.setContainsVersion(versionField != null);
			versionCursor.setResultSet(sqlConnection.selectFields(getMetaData().getFullqualifiedEscapedName(), selectSB, null, null, null, null));
			versionCursor.afterPropertiesSet();
			return versionCursor;
		}
		finally
		{
			objectCollector.dispose(selectSB);
		}
	}
}
