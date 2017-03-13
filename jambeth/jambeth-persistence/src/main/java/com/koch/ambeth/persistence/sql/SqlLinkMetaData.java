package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.LinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

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
