package de.osthus.ambeth.merge;

public interface IValueObjectConfig
{
	Class<?> getEntityType();

	Class<?> getValueType();

	String getValueObjectMemberName(String businessObjectMemberName);

	ValueObjectMemberType getValueObjectMemberType(String valueObjectMemberName);

	boolean holdsListType(String memberName);

	boolean isIgnoredMember(String memberName);

	Class<?> getMemberType(String memberName);
}