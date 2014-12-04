package de.osthus.ambeth.orm;

import java.util.regex.Matcher;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.PermissionGroup;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;

public class OrmPatternMatcher implements IInitializingBean, IOrmPatternMatcher
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = PersistenceConfigurationConstants.DatabaseTablePrefix, defaultValue = "")
	protected String tablePrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseTablePostfix, defaultValue = "")
	protected String tablePostfix;

	@Property(name = PersistenceConfigurationConstants.DatabaseArchiveTablePrefix, defaultValue = "")
	protected String archiveTablePrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseArchiveTablePostfix, defaultValue = "")
	protected String archiveTablePostfix;

	@Property(name = PersistenceConfigurationConstants.DatabasePermissionGroupPrefix, defaultValue = PermissionGroup.permGroupPrefix)
	protected String permissionGroupPrefix;

	@Property(name = PersistenceConfigurationConstants.DatabasePermissionGroupPostfix, defaultValue = PermissionGroup.permGroupSuffix)
	protected String permissionGroupPostfix;

	@Property(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, defaultValue = "")
	protected String fieldPrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseFieldPostfix, defaultValue = "")
	protected String fieldPostfix;

	@Property(name = PersistenceConfigurationConstants.DatabaseSequencePrefix, defaultValue = "")
	protected String sequencePrefix;

	@Property(name = PersistenceConfigurationConstants.DatabaseSequencePostfix, defaultValue = "")
	protected String sequencePostfix = "";

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (archiveTablePrefix.isEmpty() && archiveTablePostfix.isEmpty())
		{
			archiveTablePostfix = "_ARC";
		}
		if (sequencePrefix.isEmpty() && sequencePostfix.isEmpty())
		{
			sequencePostfix = "_SEQ";
		}
	}

	@Override
	public boolean matchesSequencePattern(String potentialSequenceName)
	{
		return potentialSequenceName.startsWith(sequencePrefix) && potentialSequenceName.endsWith(sequencePostfix);
	}

	@Override
	public String buildSequenceFromTableName(String tableName)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (matcher.matches())
		{
			tableName = matcher.group(2);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(sequencePrefix).append(tableName).append(sequencePostfix);
		return sb.toString();
	}

	@Override
	public boolean matchesFieldPattern(String potentialFieldName)
	{
		return potentialFieldName.startsWith(fieldPrefix) && potentialFieldName.endsWith(fieldPostfix);
	}

	@Override
	public boolean matchesPermissionGroupPattern(String potentialPermissionGroupName)
	{
		return potentialPermissionGroupName.startsWith(permissionGroupPrefix) && potentialPermissionGroupName.endsWith(permissionGroupPostfix);
	}

	@Override
	public String buildPermissionGroupFromTableName(String tableName)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (matcher.matches())
		{
			tableName = matcher.group(2);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(permissionGroupPrefix).append(tableName).append(permissionGroupPostfix);
		return sb.toString();
	}

	@Override
	public boolean matchesArchivePattern(String potentialArchiveName)
	{
		return potentialArchiveName.startsWith(archiveTablePrefix) && potentialArchiveName.endsWith(archiveTablePostfix);
	}

	@Override
	public String buildTableNameFromSoftName(String softName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(tablePrefix).append(softName).append(tablePostfix);
		return sb.toString();
	}

	@Override
	public String buildFieldNameFromSoftName(String softName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(fieldPrefix).append(softName).append(fieldPostfix);
		return sb.toString();
	}

	@Override
	public String buildArchiveFromTableName(String tableName)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (matcher.matches())
		{
			tableName = matcher.group(2);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(archiveTablePrefix).append(tableName).append(archiveTablePostfix);
		return sb.toString();
	}

	@Override
	public boolean matchesTablePattern(String potentialTableName)
	{
		return potentialTableName.startsWith(tablePrefix) && potentialTableName.endsWith(tablePostfix);
	}
}
