package de.osthus.ambeth.stream;

/**
 * Marker interface
 */
public interface IInputSource
{
	IInputStream deriveInputStream();
}
