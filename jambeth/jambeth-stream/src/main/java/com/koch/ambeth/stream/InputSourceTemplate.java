package com.koch.ambeth.stream;

import com.koch.ambeth.util.IImmutableType;

/**
 * Just an enum to ensure singleton pattern easily. It is marked as immutable type which would be necessary if the class would not be an enum any time in the
 * future. The only reason why it exists is to mark a primitive value of a <code>LoadContainer</code> for <code>IInputSource</code> functionality. The
 * <code>IInputSource</code> itself will be created by the <code>IConversionHelper</code> for each applied streamable property of an entity instance
 */
public enum InputSourceTemplate implements IInputSourceTemplate, IImmutableType
{
	INSTANCE;
}
