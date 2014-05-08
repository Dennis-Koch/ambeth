package de.osthus.ambeth.persistence;

import java.util.Enumeration;

import de.osthus.ambeth.util.IDisposable;

public interface IVersionResult extends Enumeration<IVersionItem>, IDisposable
{

	IVersionCursor getEnumerator();

}
