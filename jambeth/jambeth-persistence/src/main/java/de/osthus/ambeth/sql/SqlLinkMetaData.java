package de.osthus.ambeth.sql;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.LinkMetaData;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class SqlLinkMetaData extends LinkMetaData
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String constraintName;

	protected IFieldMetaData fromField;

	protected IFieldMetaData toField;

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
	public IFieldMetaData getFromField()
	{
		return fromField;
	}

	public void setFromField(IFieldMetaData fromField)
	{
		this.fromField = fromField;
	}

	@Override
	public IFieldMetaData getToField()
	{
		return toField;
	}

	public void setToField(IFieldMetaData toField)
	{
		this.toField = toField;
	}
}
