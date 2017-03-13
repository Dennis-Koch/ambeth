package com.koch.ambeth.merge.orm;

public interface ILinkConfig
{
	String getSource();

	String getAlias();

	CascadeDeleteDirection getCascadeDeleteDirection();
}