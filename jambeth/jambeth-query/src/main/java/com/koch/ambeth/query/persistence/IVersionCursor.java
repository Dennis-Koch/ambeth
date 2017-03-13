package com.koch.ambeth.query.persistence;

import com.koch.ambeth.util.IDisposable;

public interface IVersionCursor extends IDisposable {
	boolean moveNext();

	IVersionItem getCurrent();

	int getAlternateIdCount();
}
