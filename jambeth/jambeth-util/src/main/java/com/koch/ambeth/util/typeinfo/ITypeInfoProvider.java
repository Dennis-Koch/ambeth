package com.koch.ambeth.util.typeinfo;

import java.lang.reflect.Field;

public interface ITypeInfoProvider
{
	ITypeInfo getTypeInfo(Class<?> type);

	ITypeInfoItem getHierarchicMember(Class<?> entityType, String hierarchicMemberName);

	ITypeInfoProvider getInstance();

	ITypeInfoItem getMember(Class<?> entityType, IPropertyInfo propertyInfo);

	ITypeInfoItem getMember(Field field);

	ITypeInfoItem getMember(String propertyName, Field field);
}
