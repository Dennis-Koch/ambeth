package de.osthus.ambeth.sql;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IField;
import de.osthus.ambeth.persistence.ILinkCursor;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.Link;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class SqlLink extends Link
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String constraintName;

	protected IField fromField;

	protected IField toField;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Autowired
	protected ISqlConnection sqlConnection;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IConversionHelper conversionHelper;

	protected String fullqualifiedEscapedTableName;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();

		ParamChecker.assertTrue(fromField != null || toField != null, "FromField or ToField");
	}

	public void setConstraintName(String constraintName)
	{
		this.constraintName = constraintName;
	}

	@Override
	public String getFullqualifiedEscapedTableName()
	{
		return fullqualifiedEscapedTableName;
	}

	public void setFullqualifiedEscapedTableName(String fullqualifiedEscapedTableName)
	{
		this.fullqualifiedEscapedTableName = fullqualifiedEscapedTableName;
	}

	public String getConstraintName()
	{
		return constraintName;
	}

	@Override
	public IField getFromField()
	{
		return this.fromField;
	}

	public void setFromField(IField fromField)
	{
		this.fromField = fromField;
	}

	@Override
	public IField getToField()
	{
		return this.toField;
	}

	public void setToField(IField toField)
	{
		this.toField = toField;
	}

	@Override
	public ILinkCursor findAllLinked(IDirectedLink fromLink, List<?> fromIds)
	{
		return findAllLinkedIntern(fromLink, fromIds, false);
	}

	@Override
	public ILinkCursor findAllLinkedTo(IDirectedLink fromLink, List<?> toIds)
	{
		return findAllLinkedIntern(fromLink, toIds, true);
	}

	protected ILinkCursor findAllLinkedIntern(IDirectedLink fromLink, List<?> fromOrToIds, boolean isToId)
	{
		// Link
		// DirLink
		// 1) F,T WHERE F IN (?) findAllLinked
		// 2) F,T WHERE T IN (?) findAllLinkedTo isToId=true
		// RevDirLink
		// 1) T,F WHERE T IN (?) findAllLinked
		// 2) T,F WHERE F IN (?) findAllLinkedTo isToId=true
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder fieldNamesSB = current.create(StringBuilder.class);
		try
		{

			IField fromField = fromLink.getFromField();
			IField toField = fromLink.getToField();

			if (!hasLinkTable())
			{
				if (fromLink.getFromTable().equals(fromLink.getLink().getFromTable()))
				{
					toField = fromField.getTable().getIdField();
				}
				else
				{
					fromField = toField.getTable().getIdField();
				}
				// All fields themselves are correct now. But for "some" reason we have to switch the fields :(
				IField tempField = fromField;
				fromField = toField;
				toField = tempField;
			}
			sqlBuilder.appendName(fromField.getName(), fieldNamesSB);
			fieldNamesSB.append(',');
			sqlBuilder.appendName(toField.getName(), fieldNamesSB);

			String fieldNames = fieldNamesSB.toString();
			IField wantedField = isToId ? fromField : toField;
			IField whereField = isToId ? toField : fromField;

			fieldNamesSB.setLength(0);
			sqlBuilder.appendName(wantedField.getName(), fieldNamesSB);
			fieldNamesSB.append(" IS NOT NULL");
			String whereSQL = fieldNamesSB.toString();

			IResultSet resultSet = sqlConnection.createResultSet(getFullqualifiedEscapedTableName(), whereField.getName(), whereField.getFieldType(),
					fieldNames, whereSQL, fromOrToIds);

			ResultSetLinkCursor linkCursor = new ResultSetLinkCursor();
			linkCursor.setFromIdIndex(fromField.getIdIndex());
			linkCursor.setToIdIndex(toField.getIdIndex());
			linkCursor.setResultSet(resultSet);
			linkCursor.afterPropertiesSet();

			return linkCursor;
		}
		finally
		{
			current.dispose(fieldNamesSB);
		}
	}

	@Override
	public void linkIds(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		if (!getName().equals(getTableName()))
		{
			updateLinks(fromLink, fromId, toIds);
			return;
		}
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder namesSB = current.create(StringBuilder.class);
		ArrayList<Object> convertedToIds = new ArrayList<Object>();
		try
		{
			IField fromField, toField;
			if (getDirectedLink() == fromLink)
			{
				fromField = this.fromField;
				toField = this.toField;
			}
			else if (getReverseDirectedLink() == fromLink)
			{
				fromField = this.toField;
				toField = this.fromField;
			}
			else
			{
				throw new IllegalArgumentException("Invalid link " + fromLink);
			}
			Class<?> fromFieldType = fromField.getFieldType();
			Class<?> toFieldType = toField.getFieldType();
			sqlBuilder.appendName(fromField.getName(), namesSB);
			namesSB.append(',');
			sqlBuilder.appendName(toField.getName(), namesSB);

			fromId = conversionHelper.convertValueToType(fromFieldType, fromId);

			for (int a = toIds.size(); a-- > 0;)
			{
				Object toId = toIds.get(a);

				if (!addLinkModToCache(fromLink, fromId, toId))
				{
					continue;
				}
				toId = conversionHelper.convertValueToType(toFieldType, toId);
				convertedToIds.add(toId);
			}
			if (convertedToIds.size() > 0)
			{
				linkIdsIntern(namesSB.toString(), fromId, toFieldType, convertedToIds);
			}
		}
		finally
		{
			current.dispose(namesSB);
		}
	}

	protected void linkIdsIntern(String names, Object fromId, Class<?> toIdType, List<Object> toIds)
	{
		throw new UnsupportedOperationException();
	}

	protected void unlinkIdsIntern(String whereSQL, Class<?> toIdType, IMap<Integer, Object> params)
	{
		throw new UnsupportedOperationException();
	}

	public void updateLinks(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		if (toIds.size() == 1)
		{
			updateLink(fromLink, fromId, toIds.get(0));
		}
		else
		{
			// TODO!!!
			for (int i = toIds.size(); i-- > 0;)
			{
				updateLink(fromLink, fromId, toIds.get(i));
			}
			// throw new UnsupportedOperationException("Not yet implemented!");
		}
	}

	@Override
	public void updateLink(IDirectedLink fromLink, Object fromId, Object toId)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder namesAndvaluesSB = current.create(StringBuilder.class);
		StringBuilder whereSB = current.create(StringBuilder.class);
		try
		{
			if (getDirectedLink() == fromLink)
			{
			}
			else if (getReverseDirectedLink() == fromLink)
			{
				Object tempId = toId;
				toId = fromId;
				fromId = tempId;
			}
			else
			{
				throw new IllegalArgumentException("Invalid link " + fromLink);
			}

			// TODO alten im cache loeschen und neuen anlegen

			toId = conversionHelper.convertValueToType(toField.getFieldType(), toId);
			sqlBuilder.appendNameValue(toField.getName(), toId, namesAndvaluesSB);

			fromId = conversionHelper.convertValueToType(fromField.getFieldType(), fromId);
			sqlBuilder.appendNameValue(fromField.getName(), fromId, whereSB);

			sqlConnection.queueUpdate(getFullqualifiedEscapedTableName(), namesAndvaluesSB.toString(), whereSB.toString());
		}
		finally
		{
			current.dispose(namesAndvaluesSB);
			current.dispose(whereSB);
		}
	}

	@Override
	public void unlinkIds(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		if (!getName().equals(getTableName()))
		{
			unlinkByUpdate(fromLink, fromId, toIds);
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder whereSB = tlObjectCollector.create(StringBuilder.class);
		ArrayList<Object> values = new ArrayList<Object>();
		try
		{
			IField fromField;
			IField toField;

			if (getDirectedLink() == fromLink)
			{
				fromField = this.fromField;
				toField = this.toField;
			}
			else if (getReverseDirectedLink() == fromLink)
			{
				fromField = this.toField;
				toField = this.fromField;
			}
			else
			{
				throw new IllegalArgumentException("Invalid link " + fromLink);
			}
			Class<?> toFieldType = toField.getFieldType();

			for (int a = toIds.size(); a-- > 0;)
			{
				Object toId = toIds.get(a);

				if (addLinkModToCache(fromLink, fromId, toId))
				{
					values.add(conversionHelper.convertValueToType(toFieldType, toId));
				}
			}
			if (values.isEmpty())
			{
				return;
			}
			LinkedHashMap<Integer, Object> params = new LinkedHashMap<Integer, Object>();
			Class<?> fromFieldType = fromField.getFieldType();
			fromId = conversionHelper.convertValueToType(fromFieldType, fromId);

			sqlBuilder.appendName(fromField.getName(), whereSB);
			ParamsUtil.addParam(params, fromId);
			whereSB.append("=? AND ");

			persistenceHelper.appendSplittedValues(toField.getName(), toField.getFieldType(), values, params, whereSB);

			unlinkIdsIntern(whereSB.toString(), toFieldType, params);
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
		}
	}

	@Override
	public void unlinkAllIds(IDirectedLink fromLink, Object fromId)
	{
		if (!getName().equals(getTableName()))
		{
			unlinkByUpdate(fromLink, fromId, null);
			return;
		}
		StringBuilder sb = new StringBuilder();
		if (getDirectedLink() == fromLink)
		{
			fromId = conversionHelper.convertValueToType(fromField.getFieldType(), fromId);
			sqlBuilder.appendNameValue(fromField.getName(), fromId, sb);
		}
		else if (getReverseDirectedLink() == fromLink)
		{
			fromId = conversionHelper.convertValueToType(toField.getFieldType(), fromId);
			sqlBuilder.appendNameValue(toField.getName(), fromId, sb);
		}
		else
		{
			throw new IllegalArgumentException("Invalid table " + fromLink);
		}

		sqlConnection.queueDelete(getFullqualifiedEscapedTableName(), sb.toString(), null);
	}

	@Override
	public void unlinkAllIds()
	{
		if (getName().equals(getTableName()))
		{
			sqlConnection.queueDeleteAll(getFullqualifiedEscapedTableName());
		}
		else
		{
			unlinkByUpdate(null, null, null);
		}
	}

	/**
	 * 
	 * @param fromTable
	 * @param fromId
	 * @param toIds
	 *            For 1:n version
	 */
	private void unlinkByUpdate(IDirectedLink fromLink, Object fromId, List<?> toIds)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder nameAndValueSB = current.create(StringBuilder.class);
		StringBuilder whereSB = current.create(StringBuilder.class);
		IList<Object> values = null;
		try
		{
			if (getDirectedLink() != fromLink && getReverseDirectedLink() != fromLink)
			{
				throw new IllegalArgumentException("Invalid link " + fromLink);
			}

			Class<?> fromFieldType = fromField.getFieldType();
			Class<?> toFieldType = toField.getFieldType();

			sqlBuilder.appendNameValue(toField.getName(), "", nameAndValueSB);

			if (fromId != null)
			{
				fromId = conversionHelper.convertValueToType(fromFieldType, fromId);
				sqlBuilder.appendNameValue(fromField.getName(), fromId, whereSB);
			}
			if (toIds != null && !toIds.isEmpty())
			{
				values = new ArrayList<Object>();
				for (int a = toIds.size(); a-- > 0;)
				{
					Object toId = toIds.get(a);
					if (addLinkModToCache(fromLink, fromId, toId))
					{
						values.add(conversionHelper.convertValueToType(toFieldType, toId));
					}
				}
				if (!values.isEmpty())
				{
					if (fromId != null)
					{
						whereSB.append(" AND ");
					}
					sqlBuilder.appendNameValues(toField.getName(), values, whereSB);
				}
			}

			sqlConnection.queueUpdate(getFullqualifiedEscapedTableName(), nameAndValueSB.toString(), whereSB.toString());
		}
		finally
		{
			current.dispose(nameAndValueSB);
			current.dispose(whereSB);
		}
	}
}
