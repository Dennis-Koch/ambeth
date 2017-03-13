package com.koch.ambeth.persistence.orm;

public interface IOrmPatternMatcher
{
	boolean matchesSequencePattern(String potentialSequenceName);

	boolean matchesFieldPattern(String potentialFieldName);

	boolean matchesPermissionGroupPattern(String potentialPermissionGroupName);

	boolean matchesOptimisticLockTriggerPattern(String potentialOptimisticLockTriggerName);

	boolean matchesArchivePattern(String potentialArchiveName);

	boolean matchesTablePattern(String potentialTableName);

	String buildSequenceFromTableName(String tableName, int maxNameLength);

	String buildPermissionGroupFromTableName(String tableName, int maxNameLength);

	String buildOptimisticLockTriggerFromTableName(String tableName, int maxNameLength);

	String buildArchiveFromTableName(String tableName, int maxNameLength);

	String buildTableNameFromSoftName(String softName, int maxNameLength);

	String buildFieldNameFromSoftName(String softName, int maxNameLength);
}