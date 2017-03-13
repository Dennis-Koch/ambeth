package com.koch.ambeth.merge.orm;


public interface IEntityConfig
{
	Class<?> getEntityType();

	Class<?> getRealType();

	boolean isLocal();

	String getTableName();

	String getPermissionGroupName();

	String getSequenceName();

	IMemberConfig getIdMemberConfig();

	IMemberConfig getVersionMemberConfig();

	String getDescriminatorName();

	boolean isVersionRequired();

	IMemberConfig getCreatedByMemberConfig();

	IMemberConfig getCreatedOnMemberConfig();

	IMemberConfig getUpdatedByMemberConfig();

	IMemberConfig getUpdatedOnMemberConfig();

	Iterable<IMemberConfig> getMemberConfigIterable();

	Iterable<IRelationConfig> getRelationConfigIterable();
}