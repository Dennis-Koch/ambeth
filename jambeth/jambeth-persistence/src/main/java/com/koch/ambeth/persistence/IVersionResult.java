package com.koch.ambeth.persistence;

import java.util.Enumeration;

import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.util.IDisposable;

public interface IVersionResult extends Enumeration<IVersionItem>, IDisposable
{

	IVersionCursor getEnumerator();

}
