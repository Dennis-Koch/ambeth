package de.osthus.ambeth.stream;

import de.osthus.ambeth.util.IImmutableType;

/**
 * Marker interface
 */
public interface IInputSource extends IImmutableType
{
	IInputStream deriveInputStream();
}
