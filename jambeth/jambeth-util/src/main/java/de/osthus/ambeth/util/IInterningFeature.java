package de.osthus.ambeth.util;

public interface IInterningFeature
{
	<T> T intern(T value);
}