package de.osthus.ambeth.typeinfo;

public interface IRelationProvider
{
	boolean isEntityType(Class<?> type);

	String getCreatedOnMemberName();

	String getCreatedByMemberName();

	String getUpdatedOnMemberName();

	String getUpdatedByMemberName();

	String getVersionMemberName();

	String getIdMemberName();
}
