package de.osthus.ambeth.orm;

public interface ILinkConfig
{
	String getSource();

	String getAlias();

	CascadeDeleteDirection getCascadeDeleteDirection();
}