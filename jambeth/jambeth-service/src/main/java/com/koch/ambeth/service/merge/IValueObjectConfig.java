package com.koch.ambeth.service.merge;

public interface IValueObjectConfig
{
	Class<?> getEntityType();

	Class<?> getValueType();

	String getValueObjectMemberName(String businessObjectMemberName);

	String getBusinessObjectMemberName(String valueObjectMemberName);

	ValueObjectMemberType getValueObjectMemberType(String valueObjectMemberName);

	boolean holdsListType(String memberName);

	boolean isIgnoredMember(String memberName);

	Class<?> getMemberType(String memberName);
}