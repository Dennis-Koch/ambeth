package de.osthus.ambeth.persistence;

import java.util.regex.Pattern;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IPrintable;

public class PermissionGroup implements IPermissionGroup, IPrintable
{
	public static final String permGroupPrefix = "";

	public static final String permGroupSuffix = "_PG";

	public static final String permGroupIdNameOfData = "PERM_GROUP";

	public static final Pattern permGroupFieldForLink = Pattern.compile("(.+)_PG");

	public static final String permGroupIdName = "PERM_GROUP_ID";

	public static final String readPermColumName = "READ";

	public static final String updatePermColumName = "UPDATE";

	public static final String deletePermColumName = "DELETE";

	public static final String userIdName = "USER_ID";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property
	protected IField readPermissionField;

	@Property
	protected IField updatePermissionField;

	@Property
	protected IField deletePermissionField;

	@Property
	protected IField permissionGroupField;

	@Property
	protected IField userField;

	@Property
	protected IField permissionGroupFieldOnTarget;

	@Property
	protected ITable targetTable;

	@Property
	protected ITable table;

	@Override
	public ITable getTable()
	{
		return table;
	}

	@Override
	public ITable getTargetTable()
	{
		return targetTable;
	}

	@Override
	public IField getPermissionGroupFieldOnTarget()
	{
		return permissionGroupFieldOnTarget;
	}

	@Override
	public IField getUserField()
	{
		return userField;
	}

	@Override
	public IField getPermissionGroupField()
	{
		return permissionGroupField;
	}

	@Override
	public IField getReadPermissionField()
	{
		return readPermissionField;
	}

	@Override
	public IField getUpdatePermissionField()
	{
		return updatePermissionField;
	}

	@Override
	public IField getDeletePermissionField()
	{
		return deletePermissionField;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClass().getSimpleName()).append(": ").append(getTable().getName()).append(" applied to ").append(getTargetTable().getName()).append('.')
				.append(getPermissionGroupFieldOnTarget().getName());
	}
}
