package com.koch.ambeth.persistence.api;

public interface IPermissionGroup
{
	ITableMetaData getTable();

	ITableMetaData getTargetTable();

	IFieldMetaData getPermissionGroupFieldOnTarget();

	IFieldMetaData getUserField();

	IFieldMetaData getPermissionGroupField();

	IFieldMetaData getReadPermissionField();

	IFieldMetaData getUpdatePermissionField();

	IFieldMetaData getDeletePermissionField();
}
