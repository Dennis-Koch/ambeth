package com.koch.ambeth.persistence.sql;

import java.util.Iterator;

import com.koch.ambeth.persistence.api.ICursor;
import com.koch.ambeth.persistence.api.ICursorItem;

public class ResultSetCursor extends ResultSetCursorBase implements ICursor, Iterator<ICursorItem> {
	@Override
	public Iterator<ICursorItem> iterator() {
		return this;
	}
}
