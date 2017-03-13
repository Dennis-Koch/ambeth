package com.koch.ambeth.merge.copy;

/**
 * Allows to copy a StringBuilder instance efficiently
 */
public class StringBuilderOCE implements IObjectCopierExtension
{
	@Override
	public Object deepClone(Object original, IObjectCopierState objectCopierState)
	{
		StringBuilder sb = (StringBuilder) original;
		return new StringBuilder(sb);
	}
}
