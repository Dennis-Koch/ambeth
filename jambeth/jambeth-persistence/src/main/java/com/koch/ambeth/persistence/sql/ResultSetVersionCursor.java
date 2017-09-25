package com.koch.ambeth.persistence.sql;

import java.util.Iterator;

import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;

public class ResultSetVersionCursor extends ResultSetVersionCursorBase
		implements IVersionCursor, Iterator<IVersionItem> {
	@Override
	public Iterator<IVersionItem> iterator() {
		return this;
	}
}
