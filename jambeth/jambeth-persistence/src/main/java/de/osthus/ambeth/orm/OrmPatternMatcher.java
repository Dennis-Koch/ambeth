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

	protected String triggerNamePrefix = "TR_";

	protected String triggerNamePostfix = "_OL";

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

	protected String buildNameWithMaxLength(String tableName, String prefix, String postFix, int maxNameLength)
	{
		if (tableName.length() >= maxNameLength - prefix.length() - postFix.length())
		{
			return prefix + tableName.substring(0, maxNameLength - prefix.length() - postFix.length()) + postFix;
		}
		return prefix + tableName + postFix;
	}

	@Override
	public boolean matchesSequencePattern(String potentialSequenceName)
	{
		return potentialSequenceName.startsWith(sequencePrefix) && potentialSequenceName.endsWith(sequencePostfix);
	}

	@Override
	public String buildSequenceFromTableName(String tableName, int maxNameLength)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (matcher.matches())
		{
			tableName = matcher.group(2);
		}
		return buildNameWithMaxLength(tableName, sequencePrefix, sequencePostfix, maxNameLength);
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
	public boolean matchesOptimisticLockTriggerPattern(String potentialOptimisticLockTriggerName)
	{
		return potentialOptimisticLockTriggerName.startsWith(triggerNamePrefix) && potentialOptimisticLockTriggerName.endsWith(triggerNamePostfix);
	}

	@Override
	public String buildPermissionGroupFromTableName(String tableName, int maxNameLength)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (matcher.matches())
		{
			tableName = matcher.group(2);
		}
		return buildNameWithMaxLength(tableName, permissionGroupPrefix, permissionGroupPostfix, maxNameLength);
	}

	@Override
	public boolean matchesArchivePattern(String potentialArchiveName)
	{
		return potentialArchiveName.startsWith(archiveTablePrefix) && potentialArchiveName.endsWith(archiveTablePostfix);
	}

	@Override
	public String buildTableNameFromSoftName(String softName, int maxNameLength)
	{
		return buildNameWithMaxLength(softName, tablePrefix, tablePostfix, maxNameLength);
	}

	@Override
	public String buildFieldNameFromSoftName(String softName, int maxNameLength)
	{
		return buildNameWithMaxLength(softName, fieldPrefix, fieldPostfix, maxNameLength);
	}

	@Override
	public String buildArchiveFromTableName(String tableName, int maxNameLength)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (matcher.matches())
		{
			tableName = matcher.group(2);
		}
		return buildNameWithMaxLength(tableName, archiveTablePrefix, archiveTablePostfix, maxNameLength);
	}

	@Override
	public String buildOptimisticLockTriggerFromTableName(String tableName, int maxNameLength)
	{
		Matcher matcher = XmlDatabaseMapper.fqToSoftTableNamePattern.matcher(tableName);
		if (matcher.matches())
		{
			tableName = matcher.group(2);
		}
		return buildNameWithMaxLength(tableName, triggerNamePrefix, triggerNamePostfix, maxNameLength);
	}

	@Override
	public boolean matchesTablePattern(String potentialTableName)
	{
		return potentialTableName.startsWith(tablePrefix) && potentialTableName.endsWith(tablePostfix);
	}
}
