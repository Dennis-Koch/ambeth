package de.osthus.ambeth.persistence;

import java.util.Enumeration;

import de.osthus.ambeth.util.IDisposable;

public interface IResult extends Enumeration<ICursorItem>, IDisposable
{

	ICursor getEnumerator();

}
