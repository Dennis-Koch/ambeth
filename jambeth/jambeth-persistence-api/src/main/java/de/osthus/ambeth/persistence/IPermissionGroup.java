package de.osthus.ambeth.persistence;

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
