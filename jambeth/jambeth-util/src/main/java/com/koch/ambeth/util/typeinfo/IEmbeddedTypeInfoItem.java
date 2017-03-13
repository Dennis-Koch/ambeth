package com.koch.ambeth.util.typeinfo;

public interface IEmbeddedTypeInfoItem extends ITypeInfoItem
{
	ITypeInfoItem[] getMemberPath();

	String getMemberPathString();

	String[] getMemberPathToken();

	ITypeInfoItem getChildMember();
}