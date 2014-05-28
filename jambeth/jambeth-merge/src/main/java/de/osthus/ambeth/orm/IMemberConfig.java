package de.osthus.ambeth.orm;

public interface IMemberConfig extends IOrmConfig
{
	boolean isAlternateId();

	boolean isIgnore();
}