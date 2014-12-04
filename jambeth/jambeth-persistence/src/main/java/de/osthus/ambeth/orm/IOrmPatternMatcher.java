package de.osthus.ambeth.orm;

public interface IOrmPatternMatcher
{

	boolean matchesSequencePattern(String potentialSequenceName);

	String buildSequenceFromTableName(String tableName);

	boolean matchesFieldPattern(String potentialFieldName);

	boolean matchesPermissionGroupPattern(String potentialPermissionGroupName);

	String buildPermissionGroupFromTableName(String tableName);

	boolean matchesArchivePattern(String potentialArchiveName);

	String buildArchiveFromTableName(String tableName);

	boolean matchesTablePattern(String potentialTableName);

	String buildTableNameFromSoftName(String softName);

	String buildFieldNameFromSoftName(String softName);

}