package de.osthus.ambeth.persistence;

public interface IPermissionGroup
{
	ITable getTable();

	ITable getTargetTable();

	IField getPermissionGroupFieldOnTarget();

	IField getUserField();

	IField getPermissionGroupField();

	IField getReadPermissionField();
}
